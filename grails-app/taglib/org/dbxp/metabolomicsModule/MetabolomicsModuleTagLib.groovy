package org.dbxp.metabolomicsModule

import org.dbxp.dbxpModuleStorage.UploadedFile
import org.dbxp.metabolomicsModule.measurements.MeasurementPlatformVersion

/**
 * The metabolomics module tag library delivers a rich set of tags to make it easier to re-use components
 * and elements in the interface.
 *
 * @version: $Rev$
 * @author: $Author$
 * @lastrevision: $Date$
*/

class MetabolomicsModuleTagLib {

    // abbreviation for Metabolomics Module
    static namespace = "mm"

    def assayService
    def uploadedFileService

    def uploadedFileList = { attrs ->

        out << '<h1>Uploaded files</h1>'

        def uploadedFiles = uploadedFileService.getUploadedFilesForUser(session.user)

        out << uploadr.add(name: "uploadrArea", path: "/tmp", placeholder: "Drop file(s) here to upload", direction: 'up', maxVisible: 5) {

            uploadedFiles.each { uploadedFile ->

                uploadr.file(name: uploadedFile.fileName) {
                    uploadr.fileSize { uploadedFile.fileSize }
                    uploadr.fileModified { uploadedFile.lastUpdated.time }
                    uploadr.fileId { uploadedFile.id }
                }
            }

            uploadr.onSuccess {
				out << g.render(template:'/js/uploadr/onSuccess', model:[])
            }

            uploadr.onFailure {
                "console.log('failed uploading ' + file.fileName);"
            }

            uploadr.onAbort {
                "console.log('aborted uploading ' + file.fileName);"
            }

            uploadr.onView {
				out << g.render(template:'/js/uploadr/onView', model:[])
            }

            uploadr.onDownload {
				out << g.render(template:'/js/uploadr/onDownload', model:[])
            }

            uploadr.onDelete {
				out << g.render(template:'/js/uploadr/onDelete', model:[])
            }
        }
    }

    def studyList = { attrs ->

        out << '<h1>Study overview</h1>'

        // find all studies the user can read and have at least one assay
        def readableStudiesWithAssays = assayService.getAssaysReadableByUserAndGroupedByStudy(session.user)
        out << '<ul class=studyList>'

        // TODO: make assay always appear in same order
        readableStudiesWithAssays.each { study, assays ->
            out << studyTag(study: study, assays: assays)
        }

        out << '</ul>'

    }
    
    def studyTag = { attrs ->

        out << '<li class="studyTag">' + attrs.study.name + '<span class="sampleCount">' + attrs.assays.collect{it.samples?.size()}.sum() + ' samples</span><ul class="assayList">'

        attrs.assays.each { assay ->
            out << assayTag(assay: assay)
        }

        out << '</ul></li>'

    }

    def assayTag = { attrs ->

        //TODO: present the information in a better/nicer way

        def assay = attrs.assay

        def sampleMsg = "${assay.samples?.size()} samples"

        UploadedFile uploadedFile = UploadedFile.findByAssay(assay)

        def classString = "assayTag"
        def onclickString = ''


        if (uploadedFile) {
            def parsedFile = uploadedFile.parsedFile
            if (parsedFile) sampleMsg += " (${parsedFile['amountOfSamplesWithData'] ?: 0} assigned)";

            if (uploadedFile['platformVersionId']) {
                def mpv = MeasurementPlatformVersion.get((Long) uploadedFile['platformVersionId'])
                sampleMsg += " ${mpv.measurementPlatform.name} ($mpv.versionNumber)"
            }

            onclickString = "onclick=\"openParseConfigurationDialog(\' ${uploadedFile.fileName} \', ${uploadedFile.id} );\""
            classString += " clickableListItem"
        }

        out << "<li class=\"${classString}\" ${onclickString} >" + assay.name +
                '<span class=sampleCount>' + sampleMsg + '</span></li>'

    }
}
