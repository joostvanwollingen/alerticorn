package nl.vanwollingen.alerticorn.testng

import nl.vanwollingen.alerticorn.api.NotificationManager
import nl.vanwollingen.alerticorn.api.AlerticornMessage
import nl.vanwollingen.alerticorn.api.MessageFailedToSendException
import org.testng.*

class MessageListener : ITestListener {

//    override fun onTestSuccess(result: ITestResult?) {
//        val annotation = getMessageAnnotation(result)
//        annotation?.let {
//            val destinations = annotation.destination.mapNotNull { System.getenv(it) }
//            val message = toMessage(annotation, result?.throwable)
//
////            notify(annotation, message, destinations)
//        }
//    }

//    private fun notify(
//        annotation: MessageAfterTest,
//        alerticornMessage: AlerticornMessage,
//        destination: String,
//    ) {
//        try {
//            NotificationManager.notify(
//                destination = destination,
//                platform = annotation.platforms,
//                alerticornMessage,
//                destinations
//            )
//        } catch (e: MessageFailedToSendException) {
//            // If we fail to send the message, we still want to rethrow the original exception //TODO is this true for testng?
//        }
//    }

//    private fun toMessage(annotation: MessageAfterTest, throwable: Throwable?) =
//        AlerticornMessage(
//            title = annotation.title,
//            body = annotation.body,
//            details = stringArrayToMap(annotation.details),
//            links = stringArrayToMap(annotation.links),
//            throwable = throwable,
//        )
//
//    private fun getMessageAnnotation(result: ITestResult?): MessageAfterTest? {
//        val method = result?.method?.constructorOrMethod?.method
//        val annotation = method?.getAnnotation(MessageAfterTest::class.java)
//        return annotation
//    }

    private fun stringArrayToMap(stringArray: Array<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in stringArray.indices) {
            if (i % 2 == 0) {
                map[stringArray[i]] = stringArray.getOrNull(i + 1) ?: ""
            }
        }
        return map
    }

//    override fun onTestFailure(result: ITestResult?) {
//        val annotation = getMessageAnnotation(result)
//        annotation?.let {
//            val destinations = annotation.destination.mapNotNull { System.getenv(it) }
//
//            val message = toMessage(annotation,result?.throwable)
//
//            notify(annotation, message, destinations)
//        }
//    }
}