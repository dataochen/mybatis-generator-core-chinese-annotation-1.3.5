package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Github github.com/orange1438
 *
 * @author orange1438
 *         2016/10/11 23:10
 */
public class MybatisControllerPlugin extends PluginAdapter {

    private FullyQualifiedJavaType slf4jLogger;
    private FullyQualifiedJavaType slf4jLoggerFactory;
    private FullyQualifiedJavaType controllerType;
    private FullyQualifiedJavaType serviceType;
    private FullyQualifiedJavaType pojoType;
    private FullyQualifiedJavaType pojoCriteriaType;
    private FullyQualifiedJavaType listType;
    private FullyQualifiedJavaType autowired;
    private FullyQualifiedJavaType controller;
    private FullyQualifiedJavaType requestMapper;
    private FullyQualifiedJavaType ResponseBody;
    private FullyQualifiedJavaType RequestParam;
    private FullyQualifiedJavaType ModelAttribute;
    private FullyQualifiedJavaType returnType;
    private String controllerPack;
    private String controllerImplPack;
//    todo
    private String serviceImplPack;
    private String project;
    private String pojoUrl;

    private List<Method> methods;
    /**
     * 是否添加注解
     */
    private boolean enableAnnotation = true;
    private boolean enableInsert = false;
    private boolean enableInsertSelective = false;
    private boolean enableDeleteByPrimaryKey = false;
    private boolean enableDeleteByExample = false;
    private boolean enableUpdateByExample = false;
    private boolean enableUpdateByExampleSelective = false;
    private boolean enableUpdateByPrimaryKey = false;
    private boolean enableUpdateByPrimaryKeySelective = false;

    public MybatisControllerPlugin() {
        super();
        // 默认是slf4j
        slf4jLogger = new FullyQualifiedJavaType("org.slf4j.Logger");
        slf4jLoggerFactory = new FullyQualifiedJavaType("org.slf4j.LoggerFactory");
        methods = new ArrayList<Method>();
    }

    @Override
    public boolean validate(List<String> warnings) {
        if (StringUtility.stringHasValue(properties.getProperty("enableAnnotation")))
            enableAnnotation = StringUtility.isTrue(properties.getProperty("enableAnnotation"));

        String enableInsert = properties.getProperty("enableInsert");

        String enableUpdateByExampleSelective = properties.getProperty("enableUpdateByExampleSelective");

        String enableInsertSelective = properties.getProperty("enableInsertSelective");

        String enableUpdateByPrimaryKey = properties.getProperty("enableUpdateByPrimaryKey");

        String enableDeleteByPrimaryKey = properties.getProperty("enableDeleteByPrimaryKey");

        String enableDeleteByExample = properties.getProperty("enableDeleteByExample");

        String enableUpdateByPrimaryKeySelective = properties.getProperty("enableUpdateByPrimaryKeySelective");

        String enableUpdateByExample = properties.getProperty("enableUpdateByExample");

        if (StringUtility.stringHasValue(enableInsert))
            this.enableInsert = StringUtility.isTrue(enableInsert);
        if (StringUtility.stringHasValue(enableUpdateByExampleSelective))
            this.enableUpdateByExampleSelective = StringUtility.isTrue(enableUpdateByExampleSelective);
        if (StringUtility.stringHasValue(enableInsertSelective))
            this.enableInsertSelective = StringUtility.isTrue(enableInsertSelective);
        if (StringUtility.stringHasValue(enableUpdateByPrimaryKey))
            this.enableUpdateByPrimaryKey = StringUtility.isTrue(enableUpdateByPrimaryKey);
        if (StringUtility.stringHasValue(enableDeleteByPrimaryKey))
            this.enableDeleteByPrimaryKey = StringUtility.isTrue(enableDeleteByPrimaryKey);
        if (StringUtility.stringHasValue(enableDeleteByExample))
            this.enableDeleteByExample = StringUtility.isTrue(enableDeleteByExample);
        if (StringUtility.stringHasValue(enableUpdateByPrimaryKeySelective))
            this.enableUpdateByPrimaryKeySelective = StringUtility.isTrue(enableUpdateByPrimaryKeySelective);
        if (StringUtility.stringHasValue(enableUpdateByExample))
            this.enableUpdateByExample = StringUtility.isTrue(enableUpdateByExample);

        controllerPack = properties.getProperty("targetPackage");
        controllerImplPack = properties.getProperty("implementationPackage");
        serviceImplPack = properties.getProperty("implementationServicePackage");
        project = properties.getProperty("targetProject");

        pojoUrl = context.getJavaModelGeneratorConfiguration().getTargetPackage();

        if (enableAnnotation) {
            autowired = new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired");
            controller = new FullyQualifiedJavaType("org.springframework.stereotype.Controller");
            requestMapper = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping");
            ResponseBody = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.ResponseBody");
            RequestParam = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestParam");
            ModelAttribute = new FullyQualifiedJavaType("org.springframework.web.bind.annotation.ModelAttribute");
        }
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> files = new ArrayList<GeneratedJavaFile>();
        String table = introspectedTable.getBaseRecordType();
        String tableName = table.replaceAll(this.pojoUrl + ".", "");

        serviceType = new FullyQualifiedJavaType(serviceImplPack + "." + tableName + "ServiceImpl");

        // logger.info(toLowerCase(serviceType.getShortName()));
        controllerType = new FullyQualifiedJavaType(controllerImplPack + "." + tableName + "Controller");

        pojoType = new FullyQualifiedJavaType(pojoUrl + "." + tableName);

        pojoCriteriaType = new FullyQualifiedJavaType(pojoUrl + "." + "Criteria");
        listType = new FullyQualifiedJavaType("java.util.List");
        TopLevelClass topLevelClass = new TopLevelClass(controllerType);
        // 导入必要的类
        addImport(topLevelClass);

        // 实现类
        addControllerImpl(topLevelClass, introspectedTable, tableName, files);
        addLogger(topLevelClass);

        return files;
    }


    /**
     * 添加实现类
     *
     * @param introspectedTable
     * @param tableName
     * @param files
     */
    protected void addControllerImpl(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String tableName, List<GeneratedJavaFile> files) {
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);

        if (enableAnnotation) {
            topLevelClass.addAnnotation("@Controller");
            topLevelClass.addImportedType(ModelAttribute);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("@RequestMapping");
            stringBuilder.append("(\"/");
            stringBuilder.append(tableName);
            stringBuilder.append("\")");
            topLevelClass.addAnnotation(stringBuilder.toString());
            topLevelClass.addImportedType(requestMapper);
            topLevelClass.addImportedType(controller);
            topLevelClass.addImportedType(RequestParam);
        }
        // 添加引用service
        addField(topLevelClass, tableName);
        // 添加方法
//        topLevelClass.addMethod(countByExample(introspectedTable, tableName));
//        topLevelClass.addMethod(selectByPrimaryKey(introspectedTable, tableName));
//        topLevelClass.addMethod(selectByExample(introspectedTable, tableName));
        topLevelClass.addMethod(getOtherInteger("queryCount", introspectedTable, tableName, 1));
        topLevelClass.addMethod(getOtherInteger("queryDetail", introspectedTable, tableName, 2));
        topLevelClass.addMethod(getOtherInteger("queryList", introspectedTable, tableName, 1));
        topLevelClass.addMethod(getOtherInteger("delete", introspectedTable, tableName, 2));
        topLevelClass.addMethod(getOtherInteger("update", introspectedTable, tableName, 1));
        topLevelClass.addMethod(getOtherInteger("insert", introspectedTable, tableName, 1));

        /**
         * type 的意义 pojo 1 ;key 2 ;example 3 ;pojo+example 4
         */
//        if (enableDeleteByPrimaryKey) {
//            topLevelClass.addMethod(getOtherInteger("deleteByPrimaryKey", introspectedTable, tableName, 2));
//        }
//        if (enableUpdateByPrimaryKeySelective) {
//            topLevelClass.addMethod(getOtherInteger("updateByPrimaryKeySelective", introspectedTable, tableName, 1));
//
//        }
//        if (enableInsertSelective) {
//            topLevelClass.addMethod(getOtherInsertboolean("insertSelective", introspectedTable, tableName));
//        }
        // 生成文件
        GeneratedJavaFile file = new GeneratedJavaFile(topLevelClass, project, context.getJavaFormatter());
        files.add(file);
    }

    /**
     * 添加字段
     *
     * @param topLevelClass
     */
    protected void addField(TopLevelClass topLevelClass, String tableName) {
        // 添加 service
        Field field = new Field();
        field.setName(toLowerCase(serviceType.getShortName())); // 设置变量名
        topLevelClass.addImportedType(serviceType);
        field.setType(serviceType); // 类型
        field.setVisibility(JavaVisibility.PRIVATE);
        if (enableAnnotation) {
            field.addAnnotation("@Autowired");
        }
        topLevelClass.addField(field);
    }

    /**
     * 添加方法
     */
    protected Method selectByPrimaryKey(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.addAnnotation("@RequestMapping(\"/queryDetail\")");
        method.addAnnotation("@ResponseBody");
        method.setName("queryDetail");
        method.setReturnType(pojoType);
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            method.addParameter(new Parameter(type, "key"));
        } else {
            for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                StringBuilder stringBuilder = new StringBuilder("@RequestParam(\"");
                stringBuilder.append(introspectedColumn.getJavaProperty());
                stringBuilder.append("\")");
                method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty(),stringBuilder.toString()));
            }
        }
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        // method.addBodyLine("try {");
        sb.append("return this.");
        sb.append(getDaoShort());
        sb.append("selectByPrimaryKey");
        sb.append("(");
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(");");
        method.addBodyLine(sb.toString());
        // method.addBodyLine("} catch (Exception e) {");
        // method.addBodyLine("logger.error(\"Exception: \", e);");
        // method.addBodyLine("return null;");
        // method.addBodyLine("}");
        return method;
    }

    /**
     * 添加方法
     */
    protected Method countByExample(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.addAnnotation("@RequestMapping(\"/queryCount\")");
        method.addAnnotation("@ResponseBody");
        method.setName("queryCount");
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(pojoCriteriaType, "param"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("int count = this.");
        sb.append(getDaoShort());
        sb.append("countByExample");
        sb.append("(");
        sb.append("param");
        sb.append(");");
        method.addBodyLine(sb.toString());
        method.addBodyLine("logger.debug(\"count: {}\", count);");
        method.addBodyLine("return count;");
        return method;
    }

    /**
     * 添加方法
     */
    protected Method selectByExample(IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.addAnnotation("@RequestMapping(\"/queryList\")");
        method.addAnnotation("@ResponseBody");
        method.setName("queryList");
        method.setReturnType(new FullyQualifiedJavaType("List<" + tableName + ">"));
        method.addParameter(new Parameter(pojoType, "param","@ModelAttribute"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        sb.append("return this.");
        sb.append(getDaoShort());
        if (introspectedTable.hasBLOBColumns()) {
            sb.append("selectByExampleWithoutBLOBs");
        } else {
            sb.append("selectByExample");
        }
        sb.append("(");
        sb.append("param");
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * 添加方法
     */
    protected Method getOtherInteger(String methodName, IntrospectedTable introspectedTable, String tableName, int type) {
        Method method = new Method();
        StringBuilder stringBuilder = new StringBuilder("@RequestMapping(\"/");
        stringBuilder.append(methodName);
        stringBuilder.append("\")");
        method.addAnnotation(stringBuilder.toString());
        method.addAnnotation("@ResponseBody");
        method.setName(methodName);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        String params = addParams(introspectedTable, method, type);
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        // method.addBodyLine("try {");
        sb.append("return this.");
        sb.append(getDaoShort());
        if ("queryDetail".equals(methodName)) {
            sb.append("selectByPrimaryKey");
        } else if ("delete".equals(methodName)) {
            sb.append("deleteByPrimaryKey");
        }else if ("update".equals(methodName)) {
            sb.append("updateByPrimaryKeySelective");
        }else if ("insert".equals(methodName)) {
            sb.append("insertSelective");
        }

//        if (introspectedTable.hasBLOBColumns()
//                && (!"updateByPrimaryKeySelective".equals(methodName) && !"deleteByPrimaryKey".equals(methodName)
//                && !"deleteByExample".equals(methodName) && !"updateByExampleSelective".equals(methodName))) {
//            sb.append(methodName + "WithoutBLOBs");
//        } else {
//            sb.append(methodName);
//        }
        sb.append("(");
        sb.append(params);
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * 添加方法
     */
    protected Method getOtherInsertboolean(String methodName, IntrospectedTable introspectedTable, String tableName) {
        Method method = new Method();
        method.setName(methodName);
        method.setReturnType(returnType);
        method.addParameter(new Parameter(pojoType, "record"));
        method.setVisibility(JavaVisibility.PUBLIC);
        StringBuilder sb = new StringBuilder();
        if (returnType == null) {
            sb.append("this.");
        } else {
            sb.append("return this.");
        }
        sb.append(getDaoShort());
        sb.append(methodName);
        sb.append("(");
        sb.append("record");
        sb.append(");");
        method.addBodyLine(sb.toString());
        return method;
    }

    /**
     * type 的意义 pojo 1 key 2 example 3 pojo+example 4
     */
    protected String addParams(IntrospectedTable introspectedTable, Method method, int type1) {
        switch (type1) {
            case 1:
                method.addParameter(new Parameter(pojoType, "record"));
                return "record";
            case 2:
                if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                    FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
                    method.addParameter(new Parameter(type, "key"));
                } else {
                    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                        method.addParameter(new Parameter(type, introspectedColumn.getJavaProperty()));
                    }
                }
                StringBuffer sb = new StringBuffer();
                for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append(",");
                }
                sb.setLength(sb.length() - 1);
                return sb.toString();
            case 3:
                method.addParameter(new Parameter(pojoCriteriaType, "example"));
                return "example";
            case 4:

                method.addParameter(0, new Parameter(pojoType, "record"));
                method.addParameter(1, new Parameter(pojoCriteriaType, "example"));
                return "record, example";
            default:
                break;
        }
        return null;
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

    /**
     * 添加字段
     *
     * @param topLevelClass
     */
    protected void addField(TopLevelClass topLevelClass) {
        // 添加 success
        Field field = new Field();
        field.setName("success"); // 设置变量名
        field.setType(FullyQualifiedJavaType.getBooleanPrimitiveInstance()); // 类型
        field.setVisibility(JavaVisibility.PRIVATE);
        addComment(field, "执行结果");
        topLevelClass.addField(field);
        // 设置结果
        field = new Field();
        field.setName("message"); // 设置变量名
        field.setType(FullyQualifiedJavaType.getStringInstance()); // 类型
        field.setVisibility(JavaVisibility.PRIVATE);
        addComment(field, "消息结果");
        topLevelClass.addField(field);
    }

    /**
     * 添加方法
     */
    protected void addMethod(TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setSuccess");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "success"));
        method.addBodyLine("this.success = success;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
        method.setName("isSuccess");
        method.addBodyLine("return success;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setMessage");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "message"));
        method.addBodyLine("this.message = message;");
        topLevelClass.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("getMessage");
        method.addBodyLine("return message;");
        topLevelClass.addMethod(method);
    }

    /**
     * 添加方法
     */
    protected void addMethod(TopLevelClass topLevelClass, String tableName) {
        Method method2 = new Method();
        for (int i = 0; i < methods.size(); i++) {
            Method method = new Method();
            method2 = methods.get(i);
            method = method2;
            method.removeAllBodyLines();
            method.removeAnnotation();
            StringBuilder sb = new StringBuilder();
            sb.append("return this.");
            sb.append(getDaoShort());
            sb.append(method.getName());
            sb.append("(");
            List<Parameter> list = method.getParameters();
            for (int j = 0; j < list.size(); j++) {
                sb.append(list.get(j).getName());
                sb.append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(");");
            method.addBodyLine(sb.toString());
            topLevelClass.addMethod(method);
        }
        methods.clear();
    }

    /**
     * BaseUsers to baseUsers
     *
     * @param tableName
     * @return
     */
    protected String toLowerCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * BaseUsers to baseUsers
     *
     * @param tableName
     * @return
     */
    protected String toUpperCase(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * 导入需要的类
     */
    private void addImport(TopLevelClass topLevelClass) {
        topLevelClass.addImportedType(serviceType);
        topLevelClass.addImportedType(pojoType);
        topLevelClass.addImportedType(pojoCriteriaType);
        topLevelClass.addImportedType(listType);
        topLevelClass.addImportedType(slf4jLogger);
        topLevelClass.addImportedType(slf4jLoggerFactory);
        if (enableAnnotation) {
            topLevelClass.addImportedType(controller);
            topLevelClass.addImportedType(autowired);
        }
    }

    /**
     * 导入logger
     */
    private void addLogger(TopLevelClass topLevelClass) {
        Field field = new Field();
        field.setFinal(true);
        field.setInitializationString("LoggerFactory.getLogger(" + topLevelClass.getType().getShortName() + ".class)"); // 设置值
        field.setName("logger"); // 设置变量名
        field.setStatic(true);
        field.setType(new FullyQualifiedJavaType("Logger")); // 类型
        field.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(field);
    }

    private String getDaoShort() {
        return toLowerCase(serviceType.getShortName()) + ".";
    }

    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        returnType = method.getReturnType();
        return true;
    }
}
