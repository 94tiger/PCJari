package com.example.capston.pcjari

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.capston.pcjari.PC.PCListItem
import kotlinx.android.synthetic.main.activity_detailedinformation.*

/**
 * Created by KangSeungho on 2017-11-01.
 */
class DetailedInformationActivity : BaseActivity() {
    var position = 0
    lateinit var pc: PCListItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailedinformation)

        position = intent.getIntExtra(POSITION, 0)
        pc = MainActivity.pc

        supportActionBar?.setTitle(pc.title)

        di_notice.text = pc.notice
        di_address.text = pc.si + " " + pc.gu + " " + pc.dong + " " + pc.etc_juso
        di_tel.text = pc.tel
        di_cpu.text = pc.cpu
        di_ram.text = pc.ram
        di_vga.text = pc.vga
        di_per.text = pc.peripheral
        di_price.text = "1시간 당 " + pc.price + "원"

        val img_url: String = MainActivity.server + "pc_images/" + pc.icon
        Glide.with(applicationContext).load(img_url).into(imageView)

        location_mark.setOnClickListener(locationListener)
        di_tel.setOnClickListener(telListener)
        button.setOnClickListener(seatListener)
    }

    // 주소 보기 버튼 클릭
    var locationListener: View.OnClickListener = View.OnClickListener { // 위치 확인 권한 확인
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(parent, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            val send_intent = Intent(applicationContext, MapViewActivity::class.java)
            send_intent.putExtra(POSITION, position)
            startActivity(send_intent)
        }
    }

    // 전화번호 버튼 클릭
    var telListener: View.OnClickListener = View.OnClickListener {
        val tel_value: Array<String?> = pc.tel?.split("-".toRegex())!!.toTypedArray()
        var tel = "tel:"
        for (str in tel_value) tel = tel + str
        val tel_intent = Intent(Intent.ACTION_DIAL, Uri.parse(tel))
        startActivity(tel_intent)
    }
    var seatListener: View.OnClickListener? = View.OnClickListener {
        val seat_intent = Intent(application, SeatNowActivity::class.java)
        startActivity(seat_intent)
    }

    companion object {
        val POSITION: String? = "포지션"
    }
}