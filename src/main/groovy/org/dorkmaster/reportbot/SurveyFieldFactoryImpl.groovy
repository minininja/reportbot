package org.dorkmaster.reportbot

import groovy.json.JsonBuilder
import org.dorkmaster.reportbot.config.Config

import java.util.Map

class SurveyFactoryImpl implements SurveyFactory {

    Config config


    SurveyFactoryImpl(Config config) {
        this.config = config
    }

    Map mapToSurvey(def server, Map detail, String author) {
        Map result = [:]

        Config.Value authorField = config.find("servers.${server}.authorField")
        Config.Value rawField = config.find("servers.${server}.rawField")
        Config.Value fieldMapping = config.find("servers.${server}.surveyFields")

        if (fieldMapping.isNull()) {
            // failure case
        } else {
            detail.each { k, v ->
                String field = fieldMapping.asMap()."${k.replaceAll(/\W/, "").replaceAll(/\d/,"").toUpperCase()}"
                if (field) {
                    result."${field}" = v
                }
            }
            if (!authorField.isNull()) {
                result."${authorField.asString()}" = author
            }
            if (!rawField.isNull()) {
                result."${rawField.asString()}" = new JsonBuilder(detail).toString()
            }
        }

        result
    }
}

