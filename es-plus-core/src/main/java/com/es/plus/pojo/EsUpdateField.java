package com.es.plus.pojo;


import java.util.*;
/**
 * @Author: hzh
 * @Date: 2022/9/8 11:27
 * Es更新字段辅助实体类
 */
public class EsUpdateField {

    /**
     * scipt语句
     */
    private String scipt;

    /**
     * scipt参数
     */
    private Map<String, Object> sciptParams;

    public Map<String, Object> getSciptParams() {
        return sciptParams;
    }

    public String getScipt() {
        return scipt;
    }

    public void setScipt(String scipt,Map<String, Object> sciptParams) {
        this.scipt = scipt;
        this.sciptParams = sciptParams;
    }

    public List<Field> getFields() {
        return InnerClass.FIELDS;
    }

    public List<Field> getIncrementFields() {
        return InnerClass.INCREMENT_FIELDS;
    }


    public static class Field {
        private String name;
        private Object value;

        public Field(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Field field = (Field) o;
            return Objects.equals(name, field.name) && Objects.equals(value, field.value);
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }


    /**
     * 内部类   默认的scipt语句注入字段的更新
     *
     * @author hzh
     * @date 2022/09/08
     */
    public static class InnerClass {
        private static final List<Field> FIELDS = new ArrayList<>();
        private static final List<Field> INCREMENT_FIELDS = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "EsUpdateField{" +
                "scipt='" + scipt + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EsUpdateField that = (EsUpdateField) o;
        return Objects.equals(scipt, that.scipt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scipt);
    }
}
