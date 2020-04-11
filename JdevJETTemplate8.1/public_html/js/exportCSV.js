define(['ojs/ojcore', 'knockout','configs'], function (oj, ko,configs) {

    function lovList() {
        var self = this;
                if(configs.debug == 'no'){
                    console.log = function(){};
                }
        self.textData = ko.observable();
		//CSV
        
        self.exportJSON = function (info) {
                    //this is your array with data
                    //we will stringify
                    var data = JSON.stringify(info);
                    console.log(data);
                    var id = 5;//5=xlsx, 4=docx, 3 = pdf
                    var urlData = 'http://exporter.azurewebsites.net/api/export/ExportFromJSON/' + id;
                    console.log(urlData);
                    //and export            
                    Export(urlData, id, data);
                    return false;

                }
                //extension of the file
                function findExtension(id) {
                    switch (id) {
                        case 5:
                            return "xlsx";
                        case 4:
                            return "docx";
                        case 3:
                            return "pdf";
                        default:
                            return "notKnown" + id;
                    }
                }
                //step 1: POST data, obtain unique id for the export document
                //step 2: obtain file from the unique id
                function Export(urlExport, id, data) {
                    var ext = findExtension(id);

                    //step 1 - post data
                    $.ajax({
                        type: "POST",
                        url: urlExport,
                        data: JSON.stringify({'data': data}),
                        datatype: "JSON",
                        contentType: "application/json; charset=utf-8"
                    })
                            .done(function (result) {
                                //step 2 - obtain file
                        console.log(result);
                                var urlDownload = 'http://exporter.azurewebsites.net/api/export/GetFile/' + result;
                                urlDownload += "?fileName=ecdetails&extension=" + ext;
                                window.location = urlDownload;
                            })
                            .fail(function (f) {
                                alert("error:" + f.responseText);
                            });
                }
        
        
        
        self.loadData = function(urlCollection){
            $.ajax({
                        url: urlCollection,
                        type: 'GET',
                        dataType: 'json',
                        success: function (data2) {
                         //   self.tableData.removeAll();
                            //console.log(data2);
                            self.textData(JSON.stringify(data2));
                            return self.textData();

                        },
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Authorization", "Bearer " + configs.accessToken);
                    },

                        error: function (data, e) {

                            console.log("Error");
                        }
                    });
                    // console.log("self.textData(): "+self.textData());
                    
        }

        self.exportData = function(JSONData, reportTitle, ShowLabel) {
            //If JSONData is not an object then JSON.parse will parse the JSON string in an Object
             //If JSONData is not an object then JSON.parse will parse the JSON string in an Object
                    var arrData = typeof JSONData != 'object' ? JSON.parse(JSONData) : JSONData;

                    var CSV = '';
                    //Set Report title in first row or line

                    CSV += reportTitle + '\r\n\n';

                    //This condition will generate the Label/Header
                    if (ShowLabel) {
                        var row = "";

                        //This loop will extract the label from 1st index of on array
                        for (var index in arrData[0]) {

                            //Now convert each value to string and comma-seprated
                            row += index + ',';
                        }

                        row = row.slice(0, -1);

                        //append Label row with line break
                        CSV += row + '\r\n';
                    }

                    //1st loop is to extract each row
                    for (var i = 0; i < arrData.length; i++) {
                        var row = "";

                        //2nd loop will extract each column and convert it in string comma-seprated
                        for (var index in arrData[i]) {
                            if(arrData[i][index]==null || arrData[i][index]=="<anonymous>")
                            {
                                row += '"' +"    "+ '",';
                            }else{
                            row += '"' + arrData[i][index] + '",';
                        }
                        }

                        row.slice(0, row.length - 1);

                        //add a line break after each row
                        CSV += row + '\r\n';
                    }

                    if (CSV == '') {
                        alert("Invalid data");
                        return;
                    }

                    //Generate a file name
                    var fileName = reportTitle;
                    //this will remove the blank-spaces from the title and replace it with an underscore
                   // fileName += ReportTitle.replace(/ /g, "_");

                    //Initialize file format you want csv or xls
                    var uri = 'data:text/csv;charset=utf-8,' + escape(CSV);

                    // Now the little tricky part.
                    // you can use either>> window.open(uri);
                    // but this will not work in some browsers
                    // or you will not get the correct file extension    

                    //this trick will generate a temp <a /> tag
                    var link = document.createElement("a");
                    link.href = uri;

                    //set the visibility hidden so it will not effect on your web-layout
                    link.style = "visibility:hidden";
                    link.download = fileName + ".csv";

                    //this part will append the anchor tag and remove it after automatic click
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);
                }



    }

    return new lovList();
});