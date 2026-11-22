package com.example.walletview

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.components.XAxis

import java.text.NumberFormat
import java.util.Locale

import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    // Componentes de la interfaz
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var monthSpinner: Spinner

    private lateinit var tvTotalExpense: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvSelectedMonthLabel: TextView

    // Configuracion monetaria mexicana
    private val currencyFormat =
        NumberFormat.getCurrencyInstance(Locale("es", "MX")).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }

    // Presupuesto mensual de ejemplo
    private val monthlyBudget = 12000f

    // Estructura para almacenar los gastos por categoria
    private data class CategoryExpense(
        val name: String,
        val amount: Float
    )

    // Datos de demostracion de los tres meses
    private val monthlyExpenses = linkedMapOf(
        "Noviembre 2026" to listOf(
            CategoryExpense("Alimentacion", 3200f),
            CategoryExpense("Transporte", 1400f),
            CategoryExpense("Servicios", 2100f),
            CategoryExpense("Entretenimiento", 950f),
            CategoryExpense("Otros", 650f)
        ),

        "Octubre 2026" to listOf(
            CategoryExpense("Alimentacion", 2900f),
            CategoryExpense("Transporte", 1600f),
            CategoryExpense("Servicios", 1900f),
            CategoryExpense("Entretenimiento", 1100f),
            CategoryExpense("Otros", 700f)
        ),

        "Septiembre 2026" to listOf(
            CategoryExpense("Alimentacion", 2750f),
            CategoryExpense("Transporte", 1450f),
            CategoryExpense("Servicios", 1950f),
            CategoryExpense("Entretenimiento", 900f),
            CategoryExpense("Otros", 600f)
        )
    )

    // Historial de gastos de los ultimos seis meses
    private val monthLabels = listOf(
        "Jun", "Jul", "Ago", "Sep", "Oct", "Nov"
    )

    private val historicalAmounts = listOf(
        7100f, 7400f, 7900f, 7650f, 8200f, 8300f
    )

    // Colores de las categorias
    private val categoryColors = listOf(
        Color.rgb(45, 108, 223),
        Color.rgb(44, 182, 125),
        Color.rgb(244, 162, 97),
        Color.rgb(139, 92, 246),
        Color.rgb(231, 111, 81)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajustar el contenido a las barras del sistema

// Configurar contraste de los iconos del sistema
        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

// Adaptar la pantalla a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )

            // Espacio adicional para la parte inferior
            val extraBottom = (16 *
                    resources.displayMetrics.density).toInt()

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom + extraBottom
            )

            insets
        }


        // Obtener referencias de los componentes
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        monthSpinner = findViewById(R.id.spinnerMonth)

        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvSelectedMonthLabel = findViewById(R.id.tvSelectedMonthLabel)

        // Inicializar las graficas y el selector
        configurePieChart()
        configureBarChart()
        configureMonthSpinner()
    }

    // Configuracion visual de la grafica circular
    private fun configurePieChart() {

        pieChart.apply {
            description.isEnabled = false

            isUsePercentValuesEnabled = true
            isDrawHoleEnabled = true

            // Ocultar nombres dentro de los segmentos
            isDrawEntryLabelsEnabled = false

            holeRadius = 55f
            transparentCircleRadius = 60f

            isRotationEnabled = true

            centerText = "Gastos"
            centerTextSize = 13f

            legend.apply {
                isEnabled = true
                horizontalAlignment =
                    Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment =
                    Legend.LegendVerticalAlignment.BOTTOM
                orientation =
                    Legend.LegendOrientation.HORIZONTAL

                isWordWrapEnabled = true
                textSize = 10f
                textColor = Color.rgb(90, 103, 120)
            }
        }
    }

    // Generacion de la grafica circular con datos dinamicos
    private fun updatePieChart(categories: List<CategoryExpense>, total: Float) {

        val entries = categories.map { category ->
            PieEntry(category.amount, category.name)
        }

        val dataSet = PieDataSet(entries, "").apply {

            colors = categoryColors

            sliceSpace = 3f
            selectionShift = 6f

            valueTextColor = Color.WHITE
            valueTextSize = 11f

            // Mostrar porcentajes en cada segmento
            valueFormatter = PercentFormatter()
        }

        pieChart.centerText =
            "Total\n${currencyFormat.format(total.toDouble())}"

        pieChart.data = PieData(dataSet)

        pieChart.animateY(800)
    }

    // Configuracion de la grafica de barras
    private fun configureBarChart() {

        barChart.apply {

            description.isEnabled = false

            legend.isEnabled = false


            isFitBarsEnabled = true

            extraBottomOffset = 12f

            // Configuracion del eje horizontal
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM

                valueFormatter = IndexAxisValueFormatter(monthLabels)

                granularity = 1f
                labelCount = monthLabels.size

                isDrawGridLinesEnabled = false
                textColor = Color.rgb(90, 103, 120)
                textSize = 11f

                isAvoidFirstLastClippingEnabled = true
            }

            // Configuracion del eje vertical
            axisLeft.apply {
                axisMinimum = 0f

                valueFormatter = LargeValueFormatter()

                textColor = Color.rgb(90, 103, 120)
                textSize = 10f

                gridColor = Color.rgb(230, 235, 242)
            }

            axisRight.isEnabled = false
        }

        // Crear las barras de acuerdo con los datos historicos
        val entries = historicalAmounts.mapIndexed { index, amount ->
            BarEntry(index.toFloat(), amount)
        }

        val dataSet = BarDataSet(entries, "Gastos mensuales").apply {

            color = Color.rgb(45, 108, 223)

            valueTextColor = Color.rgb(23, 47, 75)
            valueTextSize = 10f

            isDrawValuesEnabled = false

            barCornerRadius = 8f
        }

        barChart.data = BarData(dataSet).apply {
            barWidth = 0.55f
        }

        barChart.animateY(800)
    }

    // Configuracion del selector de meses

    private fun configureMonthSpinner() {

        val months = monthlyExpenses.keys.toList()

        // Adaptador con diseno personalizado
        val adapter = ArrayAdapter(
            this,
            R.layout.item_month_selected,
            months
        )

        adapter.setDropDownViewResource(
            R.layout.item_month_dropdown
        )

        monthSpinner.adapter = adapter

        monthSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedMonth = months[position]
                    updateMonthlySummary(selectedMonth)
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // No se requiere ninguna accion
                }
            }

        monthSpinner.setSelection(0)
    }


    // Actualizar los importes y la grafica del mes seleccionado
    private fun updateMonthlySummary(month: String) {

        val categories = monthlyExpenses[month] ?: return

        val total = categories.sumOf {
            it.amount.toDouble()
        }.toFloat()

        val remaining = monthlyBudget - total

        // Actualizar informacion del resumen
        tvSelectedMonthLabel.text = month.uppercase(Locale("es", "MX"))

        tvTotalExpense.text =
            currencyFormat.format(total.toDouble())

        tvRemaining.text =
            currencyFormat.format(remaining.toDouble())

        // Actualizar grafica de pastel
        updatePieChart(categories, total)
    }
}
