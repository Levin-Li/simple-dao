package com.levin.commons.dao.support;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** 从 Hibernate 解析的原查询片段派生两阶段语句，不修改查询构建器。 */
final class CollectionFetchQueryPlan {
    private static final Pattern FETCH_WORD = Pattern.compile("\\bfetch\\b", Pattern.CASE_INSENSITIVE);

    final String idStatement;
    final String countStatement;
    final String fetchFrom;
    final String idPath;
    final boolean implicitSelect;
    final Class<?> rootType;

    private CollectionFetchQueryPlan(String idStatement, String countStatement, String fetchFrom, String idPath,
                                     boolean implicitSelect, Class<?> rootType) {
        this.idStatement = idStatement;
        this.countStatement = countStatement;
        this.fetchFrom = fetchFrom;
        this.idPath = idPath;
        this.implicitSelect = implicitSelect;
        this.rootType = rootType;
    }

    static boolean mayContainFetch(String statement) {
        return FETCH_WORD.matcher(statement).find();
    }

    static CollectionFetchQueryPlan create(Parts parts, Metamodel metamodel) {
        EntityType<?> root = findEntity(metamodel, parts.rootEntityName);
        if (!root.hasSingleIdAttribute() || root.getIdType().getPersistenceType() == Type.PersistenceType.EMBEDDABLE) {
            throw new IllegalArgumentException("集合抓取分页暂不支持复合主键: " + parts.rootEntityName);
        }
        if (parts.selectExpression != null && !parts.rootAlias.equals(parts.selectExpression)) {
            throw new IllegalArgumentException("集合抓取分页要求查询单个根实体，不能使用投影或 DISTINCT");
        }
        String idName = root.getId(root.getIdType().getJavaType()).getName();
        String idPath = parts.rootAlias + "." + idName;
        Map<String, ManagedType<?>> aliases = new HashMap<>();
        aliases.put(parts.rootAlias, root);
        Set<String> pluralAliases = new HashSet<>();
        List<Boolean> pluralJoins = new ArrayList<>();
        for (JoinPart join : parts.joins) {
            ResolvedPath path = resolvePath(metamodel, aliases, pluralAliases, join.path);
            if (join.fetched && path == null) {
                throw new IllegalArgumentException("无法解析集合抓取路径的实体别名: " + join.path);
            }
            pluralJoins.add(path != null && path.plural);
            if (join.alias != null && path != null && path.target != null) {
                aliases.put(join.alias, path.target);
                if (path.plural) {
                    pluralAliases.add(join.alias);
                }
            }
        }

        // LEFT 集合抓取仅负责装载时不参与 ID 页；参与条件/排序的别名保留为普通 JOIN。
        Set<String> neededAliases = new HashSet<>(parts.referencedAliases);
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < parts.joins.size(); i++) {
                JoinPart join = parts.joins.get(i);
                if (!join.fetched || !pluralJoins.get(i) || !join.leftJoin
                        || (join.alias != null && neededAliases.contains(join.alias))) {
                    int dot = join.path.indexOf('.');
                    if (dot > 0) {
                        changed |= neededAliases.add(join.path.substring(0, dot));
                    }
                }
            }
        } while (changed);

        StringBuilder idFrom = new StringBuilder(parts.rootFrom);
        StringBuilder fetchFrom = new StringBuilder(parts.rootFrom);
        for (int i = 0; i < parts.joins.size(); i++) {
            JoinPart join = parts.joins.get(i);
            fetchFrom.append(' ').append(join.text);
            if (!join.fetched || !pluralJoins.get(i) || !join.leftJoin
                    || (join.alias != null && neededAliases.contains(join.alias))) {
                idFrom.append(' ').append(join.plainText);
            }
        }
        String idStatement = "select " + idPath + " " + idFrom
                + " " + parts.whereClause + " " + parts.orderClause;
        return new CollectionFetchQueryPlan(idStatement,
                "select count(" + idPath + ") " + idFrom + " " + parts.whereClause,
                fetchFrom.toString(), idPath, parts.selectExpression == null, root.getJavaType());
    }

    String fetchStatement(String idsParameter) {
        return (implicitSelect ? "" : "select " + idPath.substring(0, idPath.lastIndexOf('.')) + " ") + fetchFrom
                + " where " + idPath + " in (:" + idsParameter + ")";
    }

    private static EntityType<?> findEntity(Metamodel metamodel, String name) {
        EntityType<?> found = null;
        for (EntityType<?> type : metamodel.getEntities()) {
            if (type.getName().equals(name) || type.getJavaType().getName().equals(name)
                    || type.getJavaType().getSimpleName().equals(name)) {
                if (found != null) {
                    throw new IllegalArgumentException("实体名称不唯一: " + name);
                }
                found = type;
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("无法解析集合抓取分页的根实体: " + name);
        }
        return found;
    }

    private static ResolvedPath resolvePath(Metamodel metamodel, Map<String, ManagedType<?>> aliases,
                                           Set<String> pluralAliases, String path) {
        String[] names = path.split("\\.");
        ManagedType<?> owner = aliases.get(names[0]);
        if (owner == null || names.length < 2) {
            return null;
        }
        boolean plural = pluralAliases.contains(names[0]);
        for (int i = 1; i < names.length; i++) {
            Attribute<?, ?> attribute = owner.getAttribute(names[i]);
            plural |= attribute.isCollection();
            Class<?> javaType = attribute.isCollection()
                    ? ((PluralAttribute<?, ?, ?>) attribute).getElementType().getJavaType()
                    : attribute.getJavaType();
            try {
                owner = metamodel.managedType(javaType);
            } catch (IllegalArgumentException ex) {
                if (i < names.length - 1) {
                    return null;
                }
                owner = null;
            }
        }
        return new ResolvedPath(owner, plural);
    }

    private static final class ResolvedPath {
        final ManagedType<?> target;
        final boolean plural;

        ResolvedPath(ManagedType<?> target, boolean plural) {
            this.target = target;
            this.plural = plural;
        }
    }

    static final class Parts {
        final String rootEntityName;
        final String rootAlias;
        final String rootFrom;
        final String selectExpression;
        final String whereClause;
        final String orderClause;
        final List<JoinPart> joins;
        final Set<String> referencedAliases;

        Parts(String rootEntityName, String rootAlias, String rootFrom, String selectExpression,
              String whereClause, String orderClause, List<JoinPart> joins, Set<String> referencedAliases) {
            this.rootEntityName = rootEntityName;
            this.rootAlias = rootAlias;
            this.rootFrom = rootFrom;
            this.selectExpression = selectExpression;
            this.whereClause = whereClause;
            this.orderClause = orderClause;
            this.joins = Collections.unmodifiableList(new ArrayList<>(joins));
            this.referencedAliases = Collections.unmodifiableSet(new HashSet<>(referencedAliases));
        }
    }

    static final class JoinPart {
        final String text;
        final String plainText;
        final String path;
        final String alias;
        final boolean fetched;
        final boolean leftJoin;

        JoinPart(String text, String plainText, String path, String alias, boolean fetched, boolean leftJoin) {
            this.text = text;
            this.plainText = plainText;
            this.path = path;
            this.alias = alias;
            this.fetched = fetched;
            this.leftJoin = leftJoin;
        }
    }
}
