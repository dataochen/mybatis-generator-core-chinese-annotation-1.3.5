package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dataochen
 * @Description 分页插件
 * @date: 2017/11/27 22:04
 */
public class CustomPagePlugin extends PluginAdapter {
    private String pojoUrl;
    private String targetPackage;
    private String customStartEndFile1;
    private String customStartEndFile1TypeStr;
    private String customStartEndFile2;
    private String customStartEndFile2TypeStr;


    private FullyQualifiedJavaType queryReqType;
    private FullyQualifiedJavaType superType;
    private FullyQualifiedJavaType customStartEndFile1Type;
    private FullyQualifiedJavaType customStartEndFile2Type;


    @Override
    public boolean validate(List<String> warnings) {
        pojoUrl = properties.getProperty("pojoUrl");
        targetPackage = properties.getProperty("targetPackage");
        customStartEndFile1 = properties.getProperty("customStartEndFile1");
        customStartEndFile1TypeStr = properties.getProperty("customStartEndFile1Type");
        customStartEndFile2 = properties.getProperty("customStartEndFile2");
        customStartEndFile2TypeStr = properties.getProperty("customStartEndFile2Type");

        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.targetPackage + ".", "");

        superType = new FullyQualifiedJavaType(targetPackage + "." + tableName);
        queryReqType = new FullyQualifiedJavaType(targetPackage + "." + tableName + "QueryReq");
        customStartEndFile1Type = customStartEndFile1TypeStr.equals("Date") ? FullyQualifiedJavaType.getDateInstance() : FullyQualifiedJavaType.getIntInstance();
        customStartEndFile2Type = customStartEndFile2TypeStr.equals("Date") ? FullyQualifiedJavaType.getDateInstance() : FullyQualifiedJavaType.getIntInstance();

        TopLevelClass topLevelClass = new TopLevelClass(queryReqType);

//        类注释
        addComment(topLevelClass, new StringBuilder(tableName).append("分页查询实体").toString());
        //导入类
        topLevelClass.addImportedType(superType);
        topLevelClass.addImportedType(queryReqType);
        topLevelClass.addImportedType(FullyQualifiedJavaType.getDateInstance());
//        实现类
        topLevelClass.setSuperClass(superType);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        addField(topLevelClass, customStartEndFile1, customStartEndFile1Type);
        addField(topLevelClass, customStartEndFile2, customStartEndFile2Type);
        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, pojoUrl, context.getJavaFormatter());
        files.add(file);
        return files;
    }

    /**
     * 添加字段
     *
     * @param topLevelClass
     */
    protected void addField(TopLevelClass topLevelClass, String fileName, FullyQualifiedJavaType fullyQualifiedJavaType) {
        if (fileName == null || fileName.length() == 0) {
            return;
        }
        // 添加 start
        Field start = new Field();
        start.setName(new StringBuilder(fileName).append("Start").toString()); // 设置变量名
        start.setType(fullyQualifiedJavaType); // 类型
        start.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(start);
        Method method = new Method();
        method.setName(new StringBuilder("set").append(fileName.replace(fileName.charAt(0),Character.toUpperCase(fileName.charAt(0)))).append("Start").toString());
//        method.setReturnType(FullyQualifiedJavaType);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(fullyQualifiedJavaType, fileName));
        method.addBodyLine(new StringBuilder(" this.").append(fileName).append(" = ").append(fileName).append(";").toString());
        topLevelClass.addMethod(method);
        // 添加 end
        Field end = new Field();
        end.setName(new StringBuilder(fileName).append("End").toString()); // 设置变量名
        end.setType(fullyQualifiedJavaType); // 类型
        end.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(end);
        Method method1 = new Method();
        method1.setName(new StringBuilder("get").append(fileName.replace(fileName.charAt(0),Character.toUpperCase(fileName.charAt(0)))).append("Start").toString());
        method1.setReturnType(fullyQualifiedJavaType);
        method1.setVisibility(JavaVisibility.PUBLIC);
        method1.addBodyLine(new StringBuilder("return this.").append(fileName).append(";").toString());
//        method1.addParameter(new Parameter(fullyQualifiedJavaType, fileName));
        topLevelClass.addMethod(method1);
    }
    protected void addComment(JavaElement field, String comment) {
        StringBuilder sb = new StringBuilder();
        field.addJavaDocLine("/**");
        sb.append(" * ");
        comment = comment.replaceAll("\n", "<br>\n\t * ");
        sb.append(comment);
        field.addJavaDocLine(sb.toString());
        field.addJavaDocLine(" */");
    }
}
