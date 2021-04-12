package vip.smartfamily.vfs.app_executors

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NameNetThreadFactory internal constructor() : ThreadFactory {
    private val group: ThreadGroup//线程组
    private val threadNumber = AtomicInteger(1)//线程数目
    private val namePrefix: String//为每个创建的线程添加的前缀

    init {
        val s = System.getSecurityManager()
        group = if (s != null)
            s.threadGroup
        else
            Thread.currentThread().threadGroup//取得线程组
        namePrefix = "net-" +
                poolNumber.getAndIncrement() +
                "-thread-"
    }

    override fun newThread(r: Runnable): Thread {
        val t = Thread(
            group, r,
            namePrefix + threadNumber.getAndIncrement(),
            0
        )//真正创建线程的地方，设置了线程的线程组及线程名
        if (t.isDaemon) {
            t.isDaemon = false
        }
        //默认是正常优先级
        if (t.priority != Thread.NORM_PRIORITY) {
            t.priority = Thread.NORM_PRIORITY
        }
        return t
    }

    companion object {
        private val poolNumber = AtomicInteger(1)//原子类，线程池编号
    }
}
