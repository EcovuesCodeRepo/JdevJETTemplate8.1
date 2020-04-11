define(['ojs/ojcore', 'knockout'], function (oj, ko) {

    var data = {
        hai: "Working",
        jsonData: function (inputObj) {
            console.log('Entered');
            var self = this;
            // self.finalObj = ko.observableArray();
            var finalObj = [];
            console.log("headerObj");

            var headerObj = [];
            var i = 0;
            //  var j = 0;
            function humanize(str) {
                var frags = str.split('_');
                for (uc = 0; uc < frags.length; uc++) {
                    frags[uc] = frags[uc].charAt(0).toUpperCase() + frags[uc].slice(1);
                }
                return frags.join(' ');
            }
            for (var key in inputObj)
            {

                var ipRow = inputObj[key];
                //  console.log(deptArray[key] );
                var hdrObj = {};
                var rowObj = [];
                var identifiedTotalCol=1;
                for (var k in ipRow)
                { 
                    //  i++
                    // rowObj=[];
                //    console.log('i: '+i);
                 //     console.log(ipRow[k]);
                    if (i == 0)
                    {
                    //    console.log('Column: ' + k);
                        hdrObj = {};
                        hdrObj.text = humanize(k); //Attribute Name
                        hdrObj.style = 'tableHeader';
                        headerObj.push(hdrObj);
                    }
                    //console.log('ipRow[k]: '+ipRow[k]);
                    var value = ipRow[k];//Attribute Value
                    
                    
                    
                    if(ipRow[k]=='Total WEBCENTER Count' || ipRow[k]=='Total EBS Count'||identifiedTotalCol>1)//Handle Total Counts
                    {
                        if(identifiedTotalCol==1)
                        value = {colSpan: 3, text: ipRow[k], style: 'tableHeader'};
                        else
                            value = {text: ipRow[k], style: 'tableHeader'};
                    
                     //   if(identifiedTotalCol==1 ||identifiedTotalCol>3)
                        rowObj.push(value);
                    
                        identifiedTotalCol++;
                    }
                    else
                        rowObj.push(value);
                       


                }
             //   console.log('rowObj: '||rowObj);
               // identifiedTotalCol=0;
                //  var j = i;
                if (i == 0)
                {//console.log('i: '+i);
                    finalObj.push(headerObj);
                }

                finalObj.push(rowObj);
                i++;


                //  break;

            }
           // console.log(JSON.stringify(finalObj));
            return finalObj;
        }
    }
    return data;


});
