**利用java attach技术做的运行期间动态修改目标进程字节码，主要作用可以用来对出入参数的检测和链路追踪日志打印等**

使用技术java，jdk/lib/tool.jar，maven

现在实现主要功能包括安装卸载，生成检测链，类隔离，检测结果日志上报等

**优势**：

- 传统的java attach做的字节码修改往往会硬编码在代码里，此项目将hook 的方法的入参打包进了固定唯一入口，并把检测和调用规则卸载json文件里，通过spi机制动态加载对对应的实现插件，选择spi的原因还有一个是开发者可以通过引入依赖后自己实现加载配置文件的规则

- 在代码中使用java -jar的方式启动核心插件，如baidu的open-rasp是使用的反射添加jar文件到类加载器中，但是这在java9以上这是默认不被允许的

- 自定义类加载器，解决与hook的java进程可能会存在依赖冲突的问题
- 采用单例作为根引导启动整个项目的启动，资源加载等
- 良好的分包，模块管理，采用maven-shade打包框架，尽可能减少依赖冲突

**不足**：

- 卸载会有一些gc根被java的jni或者Thread里面的一些参数引用，导致卸载不干净

- 个人学习和实习经验所得，能力有限

关于代码：

![17244087500121724408749547.png](https://gitee.com/feiWoSCun/drawing-bed/raw/master/id-generate/17244087500121724408749547.png)

- agent：java attach的核心实现，由目标进程的appclassloader调用，通过反射调用core的单例实现
- boot：引导启动core模块打包后的的jar文件，用来获取java -jar的启动参数，会检测当前的jdk版本选择是否添加tools.jar文件
- common：各个模块都会依赖的一些常量，日志打印等
- core：利用tools.jar提供的api添加字节码转换器用于对目标java进程的字节码修改，生成检测链等，采用单例模式引导的整个模块启动

- msg：检测日志的上报模块
- checker-spi：用来加载配置文件的检测规则，默认是groovy-engine
- checker-impl：用来构建检测链的核心实现，参考tomcat的fliter
- checker-test：提供一个Main类，用来观察它被hook后的字节码变化，会把它打印在安装目录



测试方式，代码拉下来之后，使用mvn -install 后会在各个模块生成jar文件，把e-rasp.jar,e-rasp-shade.jar,agent.jar,checker-test.jar,以及rule.json拷贝到同一个目录中，作为安装目录



cd 到安装目录

1.启动测试进程： java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8002 -jar checker-test.jar，会看到控制台有正常的未修改之前的控制台打印逻辑

2.执行hook，java -jar e-rasp.jar -inatall <安装目录> -pid <测试进程id，可以用jps命令查看>

3.成功的话，可以观察到测试进程的控制台打印已经发生变化，因为字节码已经被修改了，同时会生成log文件在安装目录，另外还把checker-test.jar的测试java文件的修改前后后输出到了当前安装目录，分别是after-load，before-load，可以使用idea对比查看



hook前后的测试Main字节码对比（反编译后）：

```java

---------------------------------------------before------------------------------------------------------------------------------
    
    
public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        System.out.println("Current process ID: " + pid);
        Main test2 = new Main();
        System.out.println("feiwoscun server start");

        while(true) {
            System.out.println("调用开始");
            doTest1();
            doTest1("feiwoscun");
            test2.doTest();
            test2.doTest("feiwoscun");
            System.out.println("调用结束");

            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException var4) {
                return;
            }
        }
    }

    public void doTest(String s) {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, "com/endpoint/rasp/MaindoTest1false(Ljava/lang/String;)V", new Object[]{this, s});
        } catch (Throwable var3) {
            if (var3.getCause() != null && var3.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var3;
            }
        }

        System.out.println("进入void doTest(String)");
    }

    public void doTest() {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, (Object[])null);
        } catch (Throwable var2) {
            if (var2.getCause() != null && var2.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var2;
            }
        }

        System.out.println("进入void doTest()");
    }

    public static void doTest1() {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, (Object[])null);
        } catch (Throwable var1) {
            if (var1.getCause() != null && var1.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var1;
            }
        }

        System.out.println("进入static void doTest1()");
    }

    public static void doTest1(String s) {
        System.out.println("进入static void doTest1(String)");
    }
}
    
    
----------------------------------------------after------------------------------------------------------------------------------
    public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        System.out.println("Current process ID: " + pid);
        Main test2 = new Main();
        System.out.println("feiwoscun server start");

        while(true) {
            System.out.println("调用开始");
            doTest1();
            doTest1("feiwoscun");
            test2.doTest();
            test2.doTest("feiwoscun");
            System.out.println("调用结束");

            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException var4) {
                return;
            }
        }
    }

    public void doTest(String s) {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, "com/endpoint/rasp/MaindoTest1false(Ljava/lang/String;)V", new Object[]{this, s});
        } catch (Throwable var3) {
            if (var3.getCause() != null && var3.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var3;
            }
        }

        System.out.println("进入void doTest(String)");
    }

    public void doTest() {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, (Object[])null);
        } catch (Throwable var2) {
            if (var2.getCause() != null && var2.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var2;
            }
        }

        System.out.println("进入void doTest()");
    }

    public static void doTest1() {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, (Object[])null);
        } catch (Throwable var1) {
            if (var1.getCause() != null && var1.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var1;
            }
        }

        System.out.println("进入static void doTest1()");
    }

    public static void doTest1(String s) {
        try {
            System.out.println("begin hook");
            ClassloaderUtil.getRaspClassLoader().loadClass("com.endpoint.rasp.engine.hook.GenerateContextHook").getMethod("defaultCheckEnter", String.class, Object[].class).invoke((Object)null, "com/endpoint/rasp/MaindoTest11true(Ljava/lang/String;)V", new Object[]{s});
        } catch (Throwable var2) {
            if (var2.getCause() != null && var2.getCause().getClass().getName().equals("com.endpoint.rasp.engine.common.exception.SecurityException")) {
                throw var2;
            }
        }

        System.out.println("进入static void doTest1(String)");
    }
}

```



控制台打印对比：

hook后：

```
调用开始
begin hook
进入static void doTest1()
begin hook
进入到方法：private static void doTest(String) ,获取到参数：feiwoscun
[INFO ] 2024-08-23 18:45:26,314 - 执行检测链结束，打印每个链检测结果，后续可以拓展成文件或者日志
null
[INFO ] 2024-08-23 18:45:26,314 - 尝试通过zeromq发送检测结果
进入static void doTest1(String)
begin hook
进入void doTest()
begin hook
进入到方法：private  void doTest(String),获取到参数：com.endpoint.rasp.Main@6aba2b86,feiwoscun
[INFO ] 2024-08-23 18:45:26,318 - 执行检测链结束，打印每个链检测结果，后续可以拓展成文件或者日志
null
[INFO ] 2024-08-23 18:45:26,318 - 尝试通过zeromq发送检测结果
进入void doTest(String)
调用结束

```

hook前：

```
调用开始
进入static void doTest1()
进入static void doTest1(String)
进入void doTest()
进入void doTest(String)
调用结束
```

