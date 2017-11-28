package org.mybatis.generator.internal.types;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.sql.Types;

/**
 * @author dataochen
 * @Description
 * @date: 2017/11/27 21:27
 */
public class TinyIntTypeResolver extends JavaTypeResolverDefaultImpl {
    public TinyIntTypeResolver() {
        super();
        typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", //$NON-NLS-1$
                new FullyQualifiedJavaType(Integer.class.getName())));
    }
}

