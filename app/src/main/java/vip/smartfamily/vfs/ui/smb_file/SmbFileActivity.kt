package vip.smartfamily.vfs.ui.smb_file

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.BassBoost
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationSet
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import de.hdodenhof.circleimageview.CircleImageView
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.app_executors.AppExecutors
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.ui.smb_file.fragment.SmbConListFragment
import vip.smartfamily.vfs.ui.smb_file.fragment.SmbFileListFragment
import vip.smartfamily.vfs.ui.smb_file.fragment.ViewPagerFragmentStateAdapter
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener
import kotlin.math.log

class SmbFileActivity : AppCompatActivity(), TopClickListener {
    /**
     * include视图
     */
    private lateinit var fileDataView: View
    private lateinit var drawerLayout: DrawerLayout

    /**
     * 头像
     */
    private lateinit var handView: CircleImageView

    /**
     * fragment 容器
     */
    private lateinit var viewPager: ViewPager2

    /**
     * fragment 容器适配器
     */
    private lateinit var viewPagerFragmentStateAdapter: ViewPagerFragmentStateAdapter

    /**
     * 页面索引
     */
    private var index = 0

    /**
     * 滑动菜单视图
     */
    private lateinit var navigationView: NavigationView
    private var toast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smb_file)

        drawerLayout = findViewById(R.id.dl_smb)
        navigationView = findViewById(R.id.nv_menu)
        fileDataView = findViewById(R.id.smb_inc_file_data)
        // 头像点击
        handView = fileDataView.findViewById(R.id.civ_head)
        handView.setOnClickListener {
            if (index == 0) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                // 为返回建清理当前页面
                val currentPage = viewPager.currentItem
                viewPager.currentItem = currentPage - 1
                viewPagerFragmentStateAdapter.delFragment(currentPage)
            }
        }

        initFileView()

        // 加号点击
        fileDataView.findViewById<ConstraintLayout>(R.id.cl_smb_inc_add).setOnClickListener {
            // 点击添加SMB连接信息。
            val layoutInflater = LayoutInflater.from(this)
            val addConView = layoutInflater.inflate(R.layout.dialog_add_con, null)
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setView(addConView)
            val dialog = dialogBuilder.create()
            dialog.setCancelable(false)
            // 背景透明
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val ipEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_ip)
            val ipTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_ip)

            val userEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_user)
            val userTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_user)

            val pawEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_paw)
            val pawTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_paw)

            val pathEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_path)
            val pathTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_path)

            val certainButton = addConView.findViewById<TextView>(R.id.tv_dia_certain)
            certainButton.setOnClickListener {
                certainButton.isEnabled = false
                var b = true
                if (ipEditText.text.toString().isEmpty()) {
                    ipTextLayout.error = resources.getString(R.string.ip_not_null)
                    b = false
                } else {
                    ipTextLayout.error = null
                }

                if (userEditText.text.toString().isEmpty()) {
                    userTextLayout.error = resources.getString(R.string.user_not_null)
                    b = false
                } else {
                    userTextLayout.error = null
                }

                if (pawEditText.text.toString().isEmpty()) {
                    pawTextLayout.error = resources.getString(R.string.paw_not_null)
                    b = false
                } else {
                    pawTextLayout.error = null
                }

                if (pathEditText.text.toString().isEmpty()) {
                    pathTextLayout.error = resources.getString(R.string.path_not_null)
                    b = false
                } else {
                    pathTextLayout.error = null
                }
                if (b) {
                    val smbClient = SMBClient()
                    AppExecutors.instance.networkIO().execute {
                        try {
                            // 连接SMB服务
                            val connection = smbClient.connect(ipEditText.text.toString())
                            // 连接验证
                            val ac = AuthenticationContext(userEditText.text.toString(), pawEditText.text.toString().toCharArray(), ipEditText.text.toString())
                            val session = connection.authenticate(ac)

                            try {
                                // 连接空间
                                val share = session.connectShare(pathEditText.text.toString())
                                share.close()
                                connection.close()
                                val smbConInfo = SmbConInfo(
                                        pathEditText.text.toString(),
                                        ipEditText.text.toString(),
                                        userEditText.text.toString(),
                                        pawEditText.text.toString(),
                                        pathEditText.text.toString()
                                )

                                if (SmbConRepository.getInstance().isExist(ipEditText.text.toString(), pathEditText.text.toString())) {
                                    Looper.prepare()
                                    toastInfo("已存在")
                                    Looper.loop()
                                } else {
                                    dialog.dismiss()
                                    SmbConRepository.getInstance().addConInfo(smbConInfo)
                                }
                            } catch (e: Exception) {
                                Looper.prepare()
                                toastInfo(e.message.toString())
                                Looper.loop()
                            }
                        } catch (e: Exception) {
                            //Log.e("e", e.toString())
                            Looper.prepare()
                            toastInfo(e.toString())
                            runOnUiThread { certainButton.isEnabled = true }
                            Looper.loop()
                        }
                    }
                } else {
                    certainButton.isEnabled = true
                }
            }
            addConView.findViewById<TextView>(R.id.tv_dia_cancel).setOnClickListener {
                dialog.dismiss()
            }
        }
    }


    private fun initFileView() {
        val fileListFragment = SmbConListFragment(this)
        viewPager = fileDataView.findViewById(R.id.vp_fragment_smb)
        viewPager.isUserInputEnabled = false
        viewPager.offscreenPageLimit = 50
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    handView.setImageResource(R.drawable.ic_launcher_background)
                } else {
                    handView.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
                }
                index = position
            }
        })
        viewPagerFragmentStateAdapter = ViewPagerFragmentStateAdapter(supportFragmentManager, lifecycle, arrayListOf(fileListFragment))
        viewPager.adapter = viewPagerFragmentStateAdapter
    }

    override fun onClickDisk(diskShare: DiskShare, smbFileInfoList: ArrayList<SmbFileTree>) {
        val smbFileListFragment = SmbFileListFragment(diskShare, smbFileInfoList, this)
        viewPagerFragmentStateAdapter.addFragment(smbFileListFragment)
        viewPager.currentItem = viewPagerFragmentStateAdapter.itemCount - 1
    }

    private fun toastInfo(info: String) {
        toast?.cancel()
        toast?.setText(info)

        toast ?: synchronized(this) {
            toast ?: Toast.makeText(this, info, Toast.LENGTH_SHORT).also {
                toast = it
            }
        }

        toast?.show()
    }
}