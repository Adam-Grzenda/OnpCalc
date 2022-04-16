package pl.put.onpcalc

import android.content.Context
import android.widget.Toast
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.math.BigDecimal.valueOf
import java.util.*


class Calculator(private val context: Context, private val viewUpdateObserver: ViewUpdateObserver) {
    enum class Operation {
        SUBTRACTION,
        ADDITION,
        DIVISION,
        MULTIPLICATION,
        EXP,
        SQ_ROOT,
        SIGN_CHANGE
    }

    enum class Action {
        AC,
        DROP,
        SWAP,
        UNDO
    }


    private val SCALE: Int = 123

    private var stack: Stack<BigDecimal> = Stack()

    private val stackStateHolder: Stack<Stack<BigDecimal>> = Stack()


    fun calculate(operation: Operation) {
        if ((stack.size < 1 && operation == Operation.SQ_ROOT) || (stack.size < 2 && operation != Operation.SQ_ROOT)) {
            displayToast("Could not perform operation, not enough values on the stack")
        } else {
            try {
                saveState(stack)
                val a = popValue()
                if (operation == Operation.SQ_ROOT) {
                    doUnaryOperation(operation, a)
                } else {
                    val b = popValue()
                    doBinaryOperation(operation, b, a)
                }
            } catch (e: ArithmeticException) {
                displayToast("Could not perform operation: ${e.message}")
            } catch (e: UnsupportedOperationException) {
                displayToast("Unsupported operation: ${e.message}")
            }
        }

    }

    fun perform(action: Action) {
        try {
            if (action != Action.UNDO) {
                saveState(stack)
            }

            when (action) {
                Action.AC -> clearValues()
                Action.DROP -> popValue()
                Action.SWAP -> swapTopValues()
                Action.UNDO -> restorePreviousState()
            }
        } catch (e: ArithmeticException) {
            e.message?.let { displayToast(it) }
        }

    }


    private fun doUnaryOperation(
        operation: Operation,
        a: BigDecimal
    ) {
        stack.clone()

        when (operation) {
            Operation.SQ_ROOT -> pushValue(a.sqrt(SCALE))
            Operation.SIGN_CHANGE -> pushValue(a.multiply(BigDecimal(-1)))
            else -> throw UnsupportedOperationException("Unexpected unary operation")
        }

    }


    private fun doBinaryOperation(
        operation: Operation,
        b: BigDecimal,
        a: BigDecimal
    ) {
        val value = when (operation) {
            Operation.SUBTRACTION -> b.minus(a)
            Operation.ADDITION -> b.plus(a)
            Operation.MULTIPLICATION -> b.multiply(a)
            Operation.DIVISION -> b.divide(a)
            Operation.EXP -> b.pow(a.intValueExact())   //todo
            else -> throw UnsupportedOperationException("Unexpected binary operation type")
        }
        pushValue(value)
    }


    fun pushValue(value: BigDecimal) {
        stack.push(value)
        sendUpdate()
    }

    private fun sendUpdate() {
        viewUpdateObserver.updateView(stack.elements().toList())
    }

    private fun popValue(): BigDecimal {
        try {
            return stack.pop()
        } catch (e: EmptyStackException) {
            throw ArithmeticException("No values on stack")
        } finally {
            sendUpdate()
        }
    }

    private fun clearValues() {
        stack.clear()
        sendUpdate()
    }

    private fun swapTopValues() {
        try {
            val a = popValue()
            val b = popValue()

            pushValue(a)
            pushValue(b)
        } catch (e: ArithmeticException) {
            throw ArithmeticException("Not enough values to swap")
        }

    }

    private fun displayToast(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    private fun saveState(stack: Stack<BigDecimal>) {
        this.stackStateHolder.push(stack.clone() as Stack<BigDecimal>?)
    }

    private fun restorePreviousState() {
        try {
            this.stack = this.stackStateHolder.pop()
            sendUpdate()
        } catch (e: EmptyStackException) {
            displayToast("Cannot undo previous operation, no previous state record found")
        }
    }
}

private fun BigDecimal.sqrt(SCALE: Int): BigDecimal { //no sqrt in java language level 8
    val A = this
    val TWO = valueOf(2)
    var x0 = BigDecimal("0")
    var x1 = BigDecimal(Math.sqrt(A.toDouble()))
    while (x0 != x1) {
        x0 = x1
        x1 = A.divide(x0, SCALE, ROUND_HALF_UP)
        x1 = x1.add(x0)
        x1 = x1.divide(TWO, SCALE, ROUND_HALF_UP)
    }
    return x1
}
