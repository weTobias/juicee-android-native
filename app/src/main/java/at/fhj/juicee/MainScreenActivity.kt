package at.fhj.juicee

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF

class MainScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        val pieChart = findViewById<PieChart>(R.id.mainScreenPieChart)
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(ContextCompat.getColor(this, R.color.pieCenter))
        pieChart.holeRadius = 48f
        pieChart.transparentCircleRadius = 0f
        val legend = pieChart.legend
        legend.isEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelTextSize(16f)

        val centerText = "80%\nHydration"
        val indexCenterText = centerText.indexOf("\n")
        val tempSpannable = SpannableString(centerText)
        tempSpannable.setSpan(RelativeSizeSpan(3f), 0 , indexCenterText, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        pieChart.centerText = tempSpannable

        setPieData(pieChart)

        val waterCount = findViewById<TextView>(R.id.mainWaterCount)
        val waterCountText = waterCount.text.toString()
        var tempNumber = 0
        var tempText = waterCountText + tempNumber.toString()
        waterCount.text = tempText

        val btnPlus = findViewById<Button>(R.id.btnMainPlus)
        val btnMinus = findViewById<Button>(R.id.btnMainMinus)

        btnPlus.setOnClickListener{
            tempNumber += 1
            tempText = waterCountText + tempNumber.toString()
            waterCount.text = tempText
        }
        btnMinus.setOnClickListener{
            if (tempNumber > 0) {
                tempNumber -= 1
                tempText = waterCountText + tempNumber.toString()
                waterCount.text = tempText
            }
        }

    }

    private fun setPieData(pieChart: PieChart){
        val drinkData = ArrayList<PieEntry>()

        drinkData.add(PieEntry(4f, "Water"))
        drinkData.add(PieEntry(3f, "Sugar"))
        drinkData.add(PieEntry(1f, "Vitamins"))
        val dataSet = PieDataSet(drinkData, "")

        val colors = listOf<Int>(ContextCompat.getColor(this, R.color.primaryBlue),
            ContextCompat.getColor(this, R.color.sugarBlue),
            ContextCompat.getColor(this, R.color.primaryYellow))



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
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateXY(700, 700)
    }
}