/*
 * Copyright 2009-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Danno Ferrin
 */
class WeatherWidgetController {

    WeatherWidgetModel model
    WeatherWidgetView view

    void mvcGroupInit(Map args) {
        createMVCGroup('SmallForecast', 'small1')
        createMVCGroup('SmallForecast', 'small2')
        createMVCGroup('SmallForecast', 'small3')
//        createMVCGroup('SmallForecast', 'small4')

    }

    def updateWeather = {evt = null ->
        doOutside {
            def observationData = loadCurrentConditions(model.location,model.celsius)
            def forecastData = loadForecast(model.location,model.celsius)

            edt {
		model.current = Float.parseFloat((forecastData.forecast.time[0].temperature.@value) as String)
                model.locationName = forecastData.location.name

                def today = forecastData.forecast.time[0]
	        model.low = (int)Float.parseFloat((today.temperature.@min) as String)
                model.high = (int)Float.parseFloat((today.temperature.@max) as String)
                model.state = observationData.weather.@icon as String
		def i=5
                (1..3).each {
                    def day = forecastData.forecast.time[i]
                    def smallModel = app.models["small$it"]

                    smallModel.day = new Date().parse("yyyy-MM-dd HH:mm:ss",(day.@from as String).replace("T"," ")).format("EEE")
                    smallModel.high = (int)Float.parseFloat(day.temperature.@max as String)
		    smallModel.state = day.symbol.@var
		    day = forecastData.forecast.time[i+4]
                    smallModel.low = (int)Float.parseFloat(day.temperature.@min as String)
                    i+=8
                }
            }
        }
    }

    def loadForecast(String location,boolean celsius) {
        XmlSlurper slurper = new XmlSlurper()
        def units=celsius?"metric":"imperial"
        //def text = new URL("http://api.wunderground.com/api/b901c399bd3014e6/forecast/q/$location"+".xml").openStream().text
        def text = new URL("http://api.openweathermap.org/data/2.5/forecast?q=$location&appid=b03448f9b317e5a4fa515312f9516547&mode=xml&units=$units&lang=es").openStream().text
        return slurper.parse(new StringReader(text))
    }

    def loadCurrentConditions(String location,boolean celsius) {
        XmlSlurper slurper = new XmlSlurper()
        def units=celsius?"metric":"imperial"
        def text = new URL("http://api.openweathermap.org/data/2.5/weather?q=$location&appid=b03448f9b317e5a4fa515312f9516547&mode=xml&units=$units&lang=es").openStream().text
        return slurper.parse(new StringReader(text))
    }



    def showPreferences = {
        if (app.views.PrefsPanel == null) {
            createMVCGroup('PrefsPanel', parent:view.hw.JDialog)
        }
    }
}
