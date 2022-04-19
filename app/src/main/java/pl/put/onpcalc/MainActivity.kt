package pl.put.onpcalc

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.stream.IntStream
import kotlin.math.roundToInt
import kotlin.streams.toList

class MainActivity : AppCompatActivity(), ViewUpdateObserver {

    private lateinit var calculator: Calculator

    private lateinit var stackView: ListView
    private lateinit var inputValueView: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private var stackViewSize: Int = 4
    private var scale: Int = 2

    private var inputValueBuilder: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.stackView = findViewById(R.id.stackView)


        this.inputValueView = findViewById(R.id.inputView)
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setupStackView()


        this.scale = sharedPreferences.getString("accuracy", "2")?.toInt()!!
        setBackgroundFromPreferences()

        this.calculator = Calculator(
            this, this,
            scale
        )
    }

    private fun setupStackView() {

        this.stackViewSize = sharedPreferences.getString("stack_display_size", "4")?.toInt()!!

        this.stackView.adapter =
            ArrayAdapter(
                this, R.layout.stack_item,
                IntStream.range(
                    1,
                    stackViewSize + 1
                ).mapToObj { a ->
                    "$a."
                }.toArray().reversed()
            )

        this.stackView.layoutParams.height =
            (32 * this.resources.displayMetrics.density * stackViewSize).roundToInt()

    }

    private fun setBackgroundFromPreferences() {
        val color: Int = when (sharedPreferences.getString("background_color", "white")) {
            "grey" -> Color.GRAY
            "white" -> Color.WHITE
            "cyan" -> Color.CYAN
            "green" -> Color.GREEN
            else -> Color.WHITE
        }
        this.stackView.setBackgroundColor(color)
        this.inputValueView.setBackgroundColor(color)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settingsButton -> {
                val intent = Intent(this, SettingsActivity::class.java).apply { }
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }

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
            calculator.pushValue(
                inputValueBuilder.toString().toBigDecimal().setScale(scale)
            )
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
        } catch (e: ArithmeticException) {
            Toast.makeText(
                this,
                "Entered value will be rounded, you can change decimal accuracy in settings",
                Toast.LENGTH_LONG
            ).show()
            calculator.pushValue(
                inputValueBuilder.toString().toBigDecimal().setScale(scale, RoundingMode.HALF_UP)
            )
        } finally {
            clearInput()
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
        val reversedValues = values.reversed()

        val stackValuesFormatted = IntStream.rangeClosed(0, stackViewSize - 1).mapToObj { i ->
            "${stackViewSize - i}. ${reversedValues.getOrElse(stackViewSize - i - 1) { "" }}"
        }.toArray()

        this.stackView.adapter =
            ArrayAdapter(
                this, R.layout.stack_item,
                stackValuesFormatted
            )

        this.stackView.layoutParams.height =
            (32 * this.resources.displayMetrics.density * stackViewSize).roundToInt()


    }

    fun onClickBack(view: View) {
        if (inputValueBuilder.isNotEmpty()) {
            inputValueBuilder.deleteAt(inputValueBuilder.length - 1)
            updateInputView(inputValueBuilder.toString())
        }
    }


}