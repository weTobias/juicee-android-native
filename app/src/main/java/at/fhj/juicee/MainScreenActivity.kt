package at.fhj.juicee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import at.fhj.juicee.models.Beverage
import at.fhj.juicee.models.BeverageConsumption
import at.fhj.juicee.models.DailyBeverageConsumption
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainScreenActivity : AppCompatActivity() {
    private val TAG : String = "MainScreenActivity"
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private val NEEDED_FLUID_AMOUNT = 2500

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        db = Firebase.firestore
        currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            val userInformationRef = db.collection("userInformation").document(currentUser!!.uid)
            userInformationRef.get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        redirectToStart()
                    } else {
                        val userId = currentUser?.uid
                        var dailyBeverageConsumption = DailyBeverageConsumption()
                        val docRef = db.collection("dailyBeverageConsumptions").document(userId.toString()).collection("dailyConsumptions").document(
                            (LocalDateTime.now()).format(
                                DateTimeFormatter.BASIC_ISO_DATE))
                        docRef.get().addOnSuccessListener { consumption ->
                            if (consumption.exists()) {
                                dailyBeverageConsumption = consumption.toObject<DailyBeverageConsumption>()!!
                                setupScreen(dailyBeverageConsumption)
                            } else {
                                docRef.set(dailyBeverageConsumption).addOnSuccessListener { setupScreen(dailyBeverageConsumption) }.addOnFailureListener { _ ->
                                    redirectToStart()
                                }
                            }
                        }.addOnFailureListener { _ ->
                            redirectToStart()
                        }
                    }
                }
                .addOnFailureListener { _ ->
                    redirectToStart()
                }
        } else {
            redirectToStart()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(applicationContext,ProfileActivity::class.java)
        when(item.itemId){
            R.id.Settings-> startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupScreen(dailyBeverageConsumption: DailyBeverageConsumption) {
        setContentView(R.layout.activity_main_screen)

        val pieChart = findViewById<PieChart>(R.id.mainScreenPieChart)
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(applicationContext.getColor(R.color.pieCenter))
        pieChart.holeRadius = 48f
        pieChart.transparentCircleRadius = 0f
        val legend = pieChart.legend
        legend.isEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelTextSize(16f)

        val centerText = "0%\nHydration"
        val indexCenterText = centerText.indexOf("\n")
        val tempSpannable = SpannableString(centerText)
        tempSpannable.setSpan(RelativeSizeSpan(3f), 0 , indexCenterText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        pieChart.centerText = tempSpannable

        val waterCount = findViewById<TextView>(R.id.mainWaterCount)
        val waterCountText = waterCount.text.toString()
        var tempNumber = 0
        dailyBeverageConsumption.consumptions.forEach {
            if (it.beverage.name == "Water") {
                tempNumber += 1
            }
        }
        var tempText = waterCountText + tempNumber.toString()
        waterCount.text = tempText

        val btnPlus = findViewById<Button>(R.id.btnMainPlus)
        val btnMinus = findViewById<Button>(R.id.btnMainMinus)

        setPieData(pieChart, dailyBeverageConsumption)

        btnPlus.setOnClickListener{
            tempNumber += 1
            db.collection("beverages").document("water").get().addOnSuccessListener { document ->
                if (document.exists()) {
                    dailyBeverageConsumption.consumptions.add(BeverageConsumption(document.toObject<Beverage>()!!, 250))
                    db.collection("dailyBeverageConsumptions").document(currentUser?.uid.toString()).collection("dailyConsumptions").document(
                        (LocalDateTime.now()).format(
                            DateTimeFormatter.BASIC_ISO_DATE)).set(dailyBeverageConsumption).addOnSuccessListener {
                                tempText = waterCountText + tempNumber.toString()
                                waterCount.text = tempText
                                setPieData(pieChart, dailyBeverageConsumption)
                            }
                }
            }.addOnFailureListener { _ ->
                redirectToStart()
            }
        }
        btnMinus.setOnClickListener{
            if (tempNumber > 0) {
                tempNumber -= 1
                db.collection("beverages").document("water").get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        for (index in 0 until dailyBeverageConsumption.consumptions.size) {
                            if (dailyBeverageConsumption.consumptions[index].beverage.name == "Water") {
                                dailyBeverageConsumption.consumptions.removeAt(index)
                                break
                            }
                        }
                        db.collection("dailyBeverageConsumptions").document(currentUser?.uid.toString()).collection("dailyConsumptions").document(
                            (LocalDateTime.now()).format(
                                DateTimeFormatter.BASIC_ISO_DATE)).set(dailyBeverageConsumption).addOnSuccessListener {
                            tempText = waterCountText + tempNumber.toString()
                            waterCount.text = tempText
                            setPieData(pieChart, dailyBeverageConsumption)
                        }
                    }
                }.addOnFailureListener { _ ->
                    redirectToStart()
                }
            }
        }
    }

    private fun redirectToStart() {
        val intent = Intent(applicationContext,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun setPieData(pieChart: PieChart, dailyBeverageConsumption: DailyBeverageConsumption){
        val drinkData = ArrayList<PieEntry>()
        val consumptionMap: MutableMap<String, Float> = mutableMapOf()
        var hydratingFluidAmount = 0f
        dailyBeverageConsumption.consumptions.forEach {
            if (consumptionMap.containsKey(it.beverage.name)) {
                consumptionMap[it.beverage.name] = consumptionMap[it.beverage.name]!! + it.consumptionInMl
            } else {
                consumptionMap[it.beverage.name] = it.consumptionInMl.toFloat()
            }
            hydratingFluidAmount += it.consumptionInMl * (it.beverage.hydrationPercentage/100)
        }
        for ((k,v) in consumptionMap) {
            drinkData.add(PieEntry(v, k))
        }

        val dataSet = PieDataSet(drinkData, "")

        val colors = listOf<Int>(applicationContext.getColor(R.color.primaryBlue),
            applicationContext.getColor(R.color.sugarBlue),
            applicationContext.getColor(R.color.primaryYellow))


        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        dataSet.colors = colors
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
        val centerText = "${((hydratingFluidAmount/NEEDED_FLUID_AMOUNT)*100).toInt()}%\nHydration"
        val indexCenterText = centerText.indexOf("\n")
        val tempSpannable = SpannableString(centerText)
        tempSpannable.setSpan(RelativeSizeSpan(3f), 0 , indexCenterText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        pieChart.centerText = tempSpannable
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateXY(700, 700)
    }
}