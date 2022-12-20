package com.levin.commons.dao.codegen.db;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CodeFile {

	private String folder;
	private String fileName;
	private String content;


}
