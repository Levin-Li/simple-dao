package com.levin.commons.dao;

import java.util.*;

/**
 * 受信的不可变记录范围。
 * <p>各分支之间取并集，同一分支内各枚举属性取交集；空许可不得解释为不限制。</p>
 */
@io.swagger.v3.oas.annotations.media.Schema(description = "服务端强制枚举数据范围，分支OR、属性AND、值IN；不属于客户端请求模型")
public final class MandatoryCondition {
    private enum State { UNRESTRICTED, DENY, LIMITED }
    private static final MandatoryCondition UNRESTRICTED = new MandatoryCondition(State.UNRESTRICTED, List.of());
    private static final MandatoryCondition DENY = new MandatoryCondition(State.DENY, List.of());
    private final State state;
    private final List<Map<String, List<?>>> branches;
    private final Set<Map<String, List<?>>> branchSet;

    private MandatoryCondition(State state, List<Map<String, List<?>>> branches) {
        this.state = state;
        this.branches = branches;
        this.branchSet = Set.copyOf(branches);
    }
    /** 不限制记录，适用于未启用此能力的实体。 */
    public static MandatoryCondition unrestricted() { return UNRESTRICTED; }
    /** 无记录可访问，生成恒假条件。 */
    public static MandatoryCondition deny() { return DENY; }
    public boolean isUnrestricted() { return state == State.UNRESTRICTED; }
    public boolean isDenied() { return state == State.DENY; }
    public List<Map<String, List<?>>> getBranches() { return branches; }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof MandatoryCondition
                && state == ((MandatoryCondition) other).state && branchSet.equals(((MandatoryCondition) other).branchSet);
    }
    @Override
    public int hashCode() { return Objects.hash(state, branchSet); }

    /**
     * 与另一份可信范围取交集，不改变任何输入分支。
     * <p>对分支做笛卡尔积，同名字段的允许值取交集；无可行组合时返回拒绝。</p>
     */
    public MandatoryCondition and(MandatoryCondition other) {
        Objects.requireNonNull(other, "other");
        if (isDenied() || other.isDenied()) return deny();
        if (isUnrestricted()) return other;
        if (other.isUnrestricted()) return this;
        List<Map<String, Collection<?>>> combined = new ArrayList<>();
        for (Map<String, List<?>> left : branches) {
            for (Map<String, List<?>> right : other.branches) {
                Map<String, Collection<?>> branch = new TreeMap<>(left);
                boolean possible = true;
                for (Map.Entry<String, List<?>> entry : right.entrySet()) {
                    Collection<?> prior = branch.get(entry.getKey());
                    if (prior == null) branch.put(entry.getKey(), entry.getValue());
                    else {
                        List<?> intersection = prior.stream().filter(entry.getValue()::contains).toList();
                        if (intersection.isEmpty()) { possible = false; break; }
                        branch.put(entry.getKey(), intersection);
                    }
                }
                if (possible) combined.add(branch);
            }
        }
        return ofBranches(combined);
    }

    /**
     * 复制并规范化枚举授权分支。
     * <p>空列表或仅含空值集合的分支表示拒绝；空映射表示无字段限制。
     * 属性仅允许直接实体属性名，值必须为枚举常量。</p>
     */
    public static MandatoryCondition ofBranches(List<? extends Map<String, ? extends Collection<?>>> branches) {
        Objects.requireNonNull(branches, "branches");
        Set<Map<String, List<?>>> result = new LinkedHashSet<>();
        for (Map<String, ? extends Collection<?>> branch : branches) {
            Objects.requireNonNull(branch, "branch");
            if (branch.isEmpty()) return unrestricted();
            Map<String, List<?>> copy = new TreeMap<>();
            boolean impossible = false;
            for (Map.Entry<String, ? extends Collection<?>> entry : branch.entrySet()) {
                String field = Objects.requireNonNull(entry.getKey(), "field");
                if (!field.matches("[A-Za-z_$][A-Za-z0-9_$]*"))
                    throw new IllegalArgumentException("Mandatory condition requires a direct entity property: " + field);
                Collection<?> values = Objects.requireNonNull(entry.getValue(), "values");
                if (values.isEmpty()) { impossible = true; break; }
                // 仅接受不可变枚举常量，客户端文本不能被解释为SQL。
                if (values.stream().anyMatch(value -> !(value instanceof Enum<?>)))
                    throw new IllegalArgumentException("Mandatory condition values must be enum constants");
                List<?> ordered = values.stream().distinct().sorted(Comparator.comparing(value -> ((Enum<?>) value).name())).toList();
                copy.put(field, ordered);
            }
            if (!impossible) result.add(Collections.unmodifiableMap(copy));
        }
        return result.isEmpty() ? deny() : new MandatoryCondition(State.LIMITED, List.copyOf(result));
    }
}
