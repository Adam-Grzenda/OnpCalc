package pl.put.onpcalc

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import kotlin.streams.toList

class MainActivity : AppCompatActivity(), ViewUpdateObserver {

    private val calculator: Calculator = Calculator(this, this)

    private lateinit var stackView: NumberPicker;
    private lateinit var inputValueView: TextView

    private var inputValueBuilder: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.stackView = findViewById(R.id.stackView)
        this.stackView.wrapSelectorWheel = false

        this.inputValueView = findViewById(R.id.inputView)
    }

    fun onClickNumber(v: View) {
        try {
            val value: String = when (v.id) {
                R.id.zero -> "0"
                R.id.one -> "1"
                R.id.two -> "2"
                R.id.three -> "3"
                R.id.four -> "4"
                R.id.five -> "5"
                R.id.six -> "6"
                R.id.seven -> "7"
                R.id.eight -> "8"
                R.id.nine -> "9"
                else -> throw UnsupportedOperationException("Input button not mapped for number")
            }
            inputValueBuilder.append(value)


        } catch (e: UnsupportedOperationException) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickOperator(v: View) {

    }

    fun onClickDrop(v: View) {

    }

    fun onClickSwap(v: View) {

    }

    fun onEnter(view: View) {}

    fun onClickClear(view: View) {}

    override fun updateView(newValue: List<BigDecimal>) {
        updateStackView(
            newValue.stream().map { bigDecimal ->
                bigDecimal.toString()
            }.toList()
        )
    }

    private fun updateStackView(values: List<String>) {
        stackView.minValue = 0
        val stackSize = values.size - 1

        if (stackView.maxValue > stackSize) {
            stackView.maxValue = if (stackSize > 0) stackSize else 0;

            if (stackView.maxValue == 0) {
                stackView.displayedValues = arrayOf("No elements")
            } else {
                stackView.displayedValues = values.toTypedArray()
            }
        } else {
            stackView.displayedValues = values.toTypedArray()
            stackView.maxValue = stackSize
        }
    }

    fun onClickBack(view: View) {}


}