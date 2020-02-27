package com.github.qqupp.scaladash

import com.github.qqupp.scaladash.okish.{Datasource, FillStyle, StackStyle, YAxisFormat, YAxisMinimum}
import com.github.qqupp.scaladash.utils.JsonTestUtils._
import io.circe.{Encoder, Json}
import io.circe.literal._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.Prop._
import org.scalacheck.magnolia._
import io.circe.syntax._

class PanelSpec extends FlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  behavior of "a Panel"

  it should "add a prometheus metric" in {
    val panel =
      Panel("test_panel")
        .withMetric(Metric.prometheusMetric("tar_get"))

    val expected: Json = json"""[{"refId": "A", "expr": "tar_get"}]"""

   panel.build(1) should containKeyValue("targets", expected)
  }

  it should "render json" in {
    forAll { (metric1: Metric, metric2: Metric, yAxis: YAxisFormat, filled: FillStyle, stacked: StackStyle, minimum: YAxisMinimum) =>


      val girdJson = json"""{
        "leftMax": null,
        "rightMax": null,
        "leftMin": $minimum,
        "rightMin": null,
        "threshold1": null,
        "threshold2": null,
        "threshold1Color": "rgba(216, 200, 27, 0.27)",
        "threshold2Color": "rgba(234, 112, 112, 0.22)"
        }"""

      val legendJson = json"""{
        "show": true,
        "values": false,
        "min": false,
        "max": false,
        "current": false,
        "total": false,
        "avg": false
      }"""

      val tooltipJson = json"""{
          "value_type": "cumulative",
          "shared": false
        }"""

      def seriesOverridesJson[T: Encoder](ts: Option[T] * ): Json =
        ts.collect { case Some(t) =>
            json"""{
                  "alias": ${t},
                  "yaxis": 2
                  }"""
        }.asJson


      val panel =
        Panel(title)
          .withMetrics(List(metric1, metric2))
          .copy(yAxisFormat = yAxis)
          .copy(filled = filled)
          .copy(stacked = stacked)
          .copy(minimum = minimum)
          .copy(span = Some(span))

      val jsonPanel = panel.build(panelId)

      jsonPanel should containKeyValue("title", title)
      jsonPanel should containKeyValue("error", false)
      jsonPanel should containKeyValue("span", span)
      jsonPanel should containKeyValue("editable", true)
      jsonPanel should containKeyValue("type", "graph")
      jsonPanel should containKeyValue("id", panelId)
      jsonPanel should containKeyValue("datasource", Json.Null)
      jsonPanel should containKeyValue("renderer", "flot")
      jsonPanel should containKeyValue("x-axis", true)
      jsonPanel should containKeyValue("y-axis", true)
      jsonPanel should containKeyValue("y_formats", List(yAxis, yAxis))
      jsonPanel should containKeyValue("grid", girdJson)
      //jsonPanel should containKeyValue("fill", "filled") to verify
      jsonPanel should containKeyValue("linewidth", 1)
      jsonPanel should containKeyValue("points", false)
      jsonPanel should containKeyValue("pointradius", 5)
      jsonPanel should containKeyValue("bars", false)
      jsonPanel should containKeyValue("stack", stacked)
      jsonPanel should containKeyValue("percentage", false)
      jsonPanel should containKeyValue("legend", legendJson)
      jsonPanel should containKeyValue("nullPointMode", "connected")
      jsonPanel should containKeyValue("steppedLine", false)
      jsonPanel should containKeyValue("tooltip", tooltipJson)
      jsonPanel should containKeyValue("targets", List(metric1.build("A"), metric2.build("B")))
      jsonPanel should containKeyValue("aliasColors", Json.arr()) // to verify list vs obj
      jsonPanel should containKeyValue("seriesOverrides", seriesOverridesJson(metric1.rightYAxisMetricName, metric2.rightYAxisMetricName))
      jsonPanel should containKeyValue("links", Json.arr())
    }
  }

  it should "render with data source" in {
    forAll { datasource: Datasource =>

      val panelJson = Panel(title).copy(datasource = Some(datasource)).build(panelId)

      panelJson should containKeyValue("datasource", datasource)
    }
  }

  it should "render lines" in {
    forAll { lines: Boolean =>
      val jsonPanel = Panel(title).copy(lines = lines).build(panelId)

      jsonPanel should containKeyValue("lines", lines)
    }
  }

  it should "render bars" in {
    forAll { bars: Boolean =>
      val jsonPanel = Panel(title).copy(bars = bars).build(panelId)

      jsonPanel should containKeyValue("bars", bars)
    }
  }

  it should "render points" in {
    forAll { points: Boolean =>
      val jsonPanel = Panel(title).copy(points = points).build(panelId)

      jsonPanel should containKeyValue("points", points)
    }
  }

  val panelId: Int = 10
  val title: String = "Test Panel"
  val span: Int = 22

}
