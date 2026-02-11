package com.lilong.tools.service.impl;


import com.lilong.blog.base.Constants;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.utils.DateUtil;
import com.lilong.blog.utils.GenUtil;
import com.lilong.blog.utils.PageUtil;
import com.lilong.tools.config.VelocityInitializer;
import com.lilong.tools.entity.GenTable;
import com.lilong.tools.entity.GenTableColumn;
import com.lilong.tools.mapper.GenTableMapper;
import com.lilong.tools.service.GenTableService;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenTableServiceImpl implements GenTableService {

    private static final String packageName = "com.lilong.blog";

    @Autowired
    private GenTableMapper genTableMapper;

    @Override
    public Map<String, Object> selectGenTableList(GenTable genTable) {
        List<GenTable> list = genTableMapper.selectGenTableList(genTable);
        Map<String, Object> result = new HashMap<>();
        result.put("rows", list);
        result.put("total", list.size());
        return result;
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new HashMap<>();
        GenTable table = genTableMapper.selectGenTableById(tableId);
        List<GenTableColumn> columns = genTableMapper.selectGenTableColumns(tableId);

        for (GenTableColumn column : columns) {
            if (column.getIsPk() != null && "1".equals(column.getIsPk())) {
                table.setPkColumn(column);
                break;
            }
        }

        // 初始化 Velocity 引擎
        VelocityInitializer.initVelocity();
        VelocityContext context = prepareContext(table, columns);

        // 获取模板列表
        List<String> templates = getTemplateList();
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            dataMap.put(getTemplateName(template), sw.toString());
        }

        return dataMap;
    }

    @Override
    @Transactional
    public int deleteGenTableByIds(Long[] tableIds) {
        genTableMapper.deleteGenTableColumnsByTableIds(tableIds);
        return genTableMapper.deleteGenTableByIds(tableIds);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncDb(String tableName) {
        try {
            // 查询表信息
            GenTable genTable = new GenTable();
            genTable.setTableName(tableName);
            List<GenTable> tableList = genTableMapper.selectDbTableList(genTable);
            if (tableList.isEmpty()) {
                throw new ServiceException("同步数据失败，原表结构不存在");
            }
            GenTable table = tableList.get(0);

            // 查询原表列信息
            List<GenTableColumn> dbColumns = genTableMapper.selectDbTableColumns(tableName);
            if (dbColumns.isEmpty()) {
                throw new ServiceException("同步数据失败，原表结构不存在");
            }

            // 查询当前已生成的列信息
            GenTable oldTable = genTableMapper.selectGenTableByName(tableName);
            List<GenTableColumn> oldColumns = genTableMapper.selectGenTableColumns(oldTable.getTableId());

            // 处理列信息
            List<String> dbColumnNames = dbColumns.stream().map(GenTableColumn::getColumnName).collect(Collectors.toList());
            List<String> oldColumnNames = oldColumns.stream().map(GenTableColumn::getColumnName).collect(Collectors.toList());

            // 删除已删除的列
            List<GenTableColumn> delColumns = oldColumns.stream()
                    .filter(column -> !dbColumnNames.contains(column.getColumnName()))
                    .collect(Collectors.toList());
            if (!delColumns.isEmpty()) {
                genTableMapper.deleteGenTableColumns(delColumns.stream()
                        .map(GenTableColumn::getColumnId)
                        .collect(Collectors.toList()));
            }

            // 新增新增的列
            List<GenTableColumn> addColumns = dbColumns.stream()
                    .filter(column -> !oldColumnNames.contains(column.getColumnName()))
                    .collect(Collectors.toList());
            addColumns.forEach(column -> {
                initColumnField(column, oldTable);
            });
            if (!addColumns.isEmpty()) {
                genTableMapper.insertGenTableBatch(addColumns);
            }

            // 更新修改的列
            List<GenTableColumn> updateColumns = dbColumns.stream()
                    .filter(column -> oldColumnNames.contains(column.getColumnName()))
                    .collect(Collectors.toList());
            updateColumns.forEach(column -> {
                GenTableColumn oldColumn = oldColumns.stream()
                        .filter(c -> c.getColumnName().equals(column.getColumnName()))
                        .findFirst()
                        .orElse(null);
                if (oldColumn != null) {
                    column.setColumnId(oldColumn.getColumnId());
                    column.setTableId(oldTable.getTableId());
                    // 保持已有的配置信息
                    column.setIsList(oldColumn.getIsList());
                    column.setIsQuery(oldColumn.getIsQuery());
                    column.setIsEdit(oldColumn.getIsEdit());
                    column.setIsInsert(oldColumn.getIsInsert());
                    column.setQueryType(oldColumn.getQueryType());
                    column.setHtmlType(oldColumn.getHtmlType());
                    // 更新数据类型相关的信息
                    column.setJavaType(GenUtil.getJavaType(column.getColumnType()));
                    column.setJavaField(GenUtil.toCamelCase(column.getColumnName()));
                }
            });
            if (!updateColumns.isEmpty()) {
                genTableMapper.updateGenTableColumns(updateColumns);
            }

            // 更新表信息
            oldTable.setTableComment(table.getTableComment());
            oldTable.setUpdateTime(new Date());
            genTableMapper.updateGenTable(oldTable);

            return "同步成功";
        } catch (Exception e) {
            throw new ServiceException("同步失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> selectDbTableList(GenTable genTable) {
        // 计算偏移量
        int offset = (PageUtil.getPageQuery().getPageNum() - 1) * PageUtil.getPageQuery().getPageSize();
        // 设置分页参数
        genTable.setOffset(offset);
        genTable.setPageSize(PageUtil.getPageQuery().getPageSize());

        // 查询数据
        List<GenTable> list = genTableMapper.selectDbTableList(genTable);
        // 查询总数
        int total = genTableMapper.selectDbTableCount(genTable);

        Map<String, Object> result = new HashMap<>();
        result.put("rows", list);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importGenTable(String[] tableNames) {
        try {
            GenTable genTable = new GenTable();
            for (String tableName : tableNames) {
                genTable.setTableName(tableName);
                // 查询表信息
                List<GenTable> tableList = genTableMapper.selectDbTableList(genTable);
                if (tableList.isEmpty()) {
                    throw new ServiceException("表" + tableName + "不存在");
                }
                GenTable table = tableList.get(0);

                // 保存表信息
                genTableMapper.insertGenTable(table);

                // 查询列信息
                List<GenTableColumn> columns = genTableMapper.selectDbTableColumns(tableName);
                for (GenTableColumn column : columns) {
                    // 设置列的默认属性
                    initColumnField(column, table);
                }

                // 批量保存列信息
                if (!columns.isEmpty()) {
                    genTableMapper.insertGenTableBatch(columns);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 初始化列属性字段
     */
    private void initColumnField(GenTableColumn column, GenTable table) {
        String dataType = column.getColumnType();
        String columnName = column.getColumnName();

        // 设置java字段名
        column.setTableId(table.getTableId());
        column.setJavaField(GenUtil.toCamelCase(columnName));

        // 设置默认类型
        column.setJavaType(GenUtil.getJavaType(dataType));

        // 设置默认显示类型
        column.setQueryType("EQ");
        column.setHtmlType("input");

        // 主键字段设置
        if ("PRI".equals(column.getColumnKey())) {
            column.setIsPk("1");
            column.setIsRequired("1");
            column.setIsInsert("1");
            table.setPkColumn(column);
        } else {
            column.setIsPk("0");
            column.setIsRequired("0");
        }

        // 设置默认操作类型
        column.setIsInsert("1");
        column.setIsEdit("1");
        column.setIsList("1");
        column.setIsQuery("1");
    }

    private List<String> getTemplateList() {
        List<String> templates = new ArrayList<>();
        templates.add("templates/entity.java.vm");
        templates.add("templates/mapper.java.vm");
        templates.add("templates/service.java.vm");
        templates.add("templates/serviceImpl.java.vm");
        templates.add("templates/controller.java.vm");
        templates.add("templates/mapper.xml.vm");
        templates.add("templates/vue.vue.vm");
        templates.add("templates/api.ts.vm");
        return templates;
    }

    private String getTemplateName(String template) {
        String[] arr = template.split("/");
        String name = arr[arr.length - 1];
        return name.substring(0, name.indexOf(".vm"));
    }

    @Override
    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);

        try {
            for (String tableName : tableNames) {
                // 查询表信息
                GenTable table = genTableMapper.selectGenTableByName(tableName);
                if (table == null) {
                    throw new RuntimeException("表" + tableName + "不存在");
                }

                // 查询列信息
                List<GenTableColumn> columns = genTableMapper.selectGenTableColumns(table.getTableId());

                // 生成代码
                VelocityInitializer.initVelocity();
                VelocityContext context = prepareContext(table, columns);

                // 获取模板列表
                List<String> templates = getTemplateList();
                for (String template : templates) {
                    // 渲染模板
                    StringWriter sw = new StringWriter();
                    Template tpl = Velocity.getTemplate(template, Constants.UTF8);
                    tpl.merge(context, sw);

                    // 获取生成的文件名
                    String fileName = getFileName(template, table, packageName);
                    if (fileName != null) {
                        // 添加到zip
                        zip.putNextEntry(new ZipEntry(fileName));
                        IOUtils.write(sw.toString(), zip, Constants.UTF8);
                        IOUtils.closeQuietly(sw);
                        zip.flush();
                        zip.closeEntry();
                    }
                }
            }

            IOUtils.closeQuietly(zip);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成代码失败", e);
        } finally {
            IOUtils.closeQuietly(zip);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private String getFileName(String template, GenTable table, String packageName) {
        String className = getClassName(table.getTableName());
        packageName = packageName.replace(".", "/");

        if (template.contains("entity.java.vm")) {
            return packageName + "/entity/" + className + ".java";
        } else if (template.contains("mapper.java.vm")) {
            return packageName + "/mapper/" + className + "Mapper.java";
        } else if (template.contains("service.java.vm")) {
            return packageName + "/service/" + className + "Service.java";
        } else if (template.contains("serviceImpl.java.vm")) {
            return packageName + "/service/impl/" + className + "ServiceImpl.java";
        } else if (template.contains("controller.java.vm")) {
            return packageName + "/controller/" + className + "Controller.java";
        } else if (template.contains("mapper.xml.vm")) {
            return "mapper/" + className + "Mapper.xml";
        } else if (template.contains("vue.vue.vm")) {
            return "vue/" + className.toLowerCase() + "/index.vue";
        }
        return null;
    }


    public static VelocityContext prepareContext(GenTable genTable, List<GenTableColumn> columns) {
        VelocityContext context = new VelocityContext();

        String moduleName = genTable.getModuleName();
        String businessName = getBusinessName(genTable.getTableName());
        String className = getClassName(genTable.getTableName());

        context.put("tableName", genTable.getTableName());
        context.put("tableComment", genTable.getTableComment());
        context.put("primaryKey", genTable.getPkColumn());
        context.put("className", className);
        context.put("classname", uncapitalize(className));
        context.put("moduleName", moduleName);
        context.put("BusinessName", capitalize(businessName));
        context.put("businessName", businessName);
        context.put("basePackage", packageName);
        context.put("packageName", packageName);
        context.put("author", "neat");
        context.put("datetime", DateUtil.getDate());
        context.put("columns", columns);

        return context;
    }

    public static String getBusinessName(String tableName) {
        String businessName = convertToCamelCase(tableName);
        return uncapitalize(businessName);
    }

    /**
     * 获取类名
     */
    public static String getClassName(String tableName) {
        String className = convertToCamelCase(tableName);
        return capitalize(className);
    }

    /**
     * 将表名转换为Java类名
     */
    public static String convertClassName(String tableName) {
        return convertToCamelCase(tableName);
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     * 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }

        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 处理真正的驼峰片段
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }


    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }


}
