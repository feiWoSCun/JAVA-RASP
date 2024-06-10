package rpc.bean;

import java.util.List;

/**
 * 内存马白名单，默认对类名添加白名单，没有类名的对非空路径添加白名单，两者都没的，对内存马类型(behinder,godzilla,tomcat,jetty,weblogic,spring,resin)添加白名单。
 * 关闭某种Web容器的检测，等价于设定了Web容器白名单
 *
 * Created by yunchao.zheng on 2023-10-11
 */
public class MemShellWhiteConfig {

    /**
     *  加白
     */
    private List<String> classNames;

    /**
     * 加白路径
     */
    private List<String> classPaths;

    /**
     * 加白内存马检测模型类型：behinder，godzilla,tomcat,jetty,weblogic,spring,resin
     */
    private List<String> types;

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public List<String> getClassPaths() {
        return classPaths;
    }

    public void setClassPaths(List<String> classPaths) {
        this.classPaths = classPaths;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
