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
        this.stackView.displayedValues = arrayOf("No elements")

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
                R.id.dot -> "." //todo do not allow a dot following another dot
                else -> throw UnsupportedOperationException("Input button not mapped for a number")
            }
            inputValueBuilder.append(value)
            updateInputView(inputValueBuilder.toString());

        } catch (e: UnsupportedOperationException) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickOperator(v: View) {
        try {
            val operation: Calculator.Operation = when (v.id) {
                R.id.plus -> Calculator.Operation.ADDITION
                R.id.minus -> Calculator.Operation.SUBTRACTION
                R.id.multiply -> Calculator.Operation.MULTIPLICATION
                R.id.divide -> Calculator.Operation.DIVISION
                R.id.exp -> Calculator.Operation.EXP
                R.id.squareRoot -> Calculator.Operation.SQ_ROOT
                R.id.sign_change -> Calculator.Operation.SIGN_CHANGE
                else -> throw UnsupportedOperationException("Input button not mapped for an operation")
            }
            calculator.calculate(operation)
        } catch (e: UnsupportedOperationException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickAction(v: View) {
        try {
            val action: Calculator.Action = when (v.id) {
                R.id.AC -> Calculator.Action.AC
                R.id.drop -> Calculator.Action.DROP
                R.id.swap -> Calculator.Action.SWAP
                R.id.undo -> Calculator.Action.UNDO
                else -> throw UnsupportedOperationException("Input button not mapped for an action")
            }
            calculator.perform(action)
        } catch (e: UnsupportedOperationException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onEnter(view: View) {
        try {
            calculator.pushValue(inputValueBuilder.toString().toBigDecimal())
            clearInput()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInput() {
        this.inputValueBuilder.clear()
        updateInputView("")
    }

    private fun updateInputView(inputValue: String) {
        this.inputValueView.text = inputValue
    }

    override fun updateView(newValue: List<BigDecimal>) {
        updateStackView(
            newValue.stream().map { bigDecimal ->
                bigDecimal.toString()
            }.toList()
        )
    }

    private fun updateStackView(values: List<String>) {
        stackView.minValue = 0
        val newMaxValue = values.size - 1

        if (stackView.maxValue >= newMaxValue) {
            stackView.maxValue = if (newMaxValue > 0) newMaxValue else 0;

            if (values.size == 0) {
                stackView.displayedValues = arrayOf("No elements")
            } else {
                stackView.displayedValues = values.toTypedArray()
            }
        } else {
            stackView.displayedValues = values.toTypedArray()
            stackView.maxValue = newMaxValue
        }
    }

    fun onClickBack(view: View) {
        if (inputValueBuilder.isNotEmpty()) {
            inputValueBuilder.deleteAt(inputValueBuilder.length - 1)
            updateInputView(inputValueBuilder.toString())
        }
    }


}