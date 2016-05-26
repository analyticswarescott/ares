/**
 * Created by scott on 17/09/15.
 */

/**
 * Created by scott on 5/4/2015.
 */

var baseURL;
var accessToken;
var serviceHost;
var servicePort;
var connected = false;

var currtid;
var force;
var svg;

var link;
var node;
//var doForce = true;

function startTest() {
    d3.json(baseURL + "/rest/1.0/tools/tenant/test/start/" + currtid ).header("accessToken", accessToken).get( function (error, data) {

        //stop the force layout
       // force.stop();
       // doForce = false;

        console.log("test start: " + JSON.stringify(data));
    });
}


function stopTest() {
    d3.json(baseURL + "/rest/1.0/tools/tenant/test/stop/" + currtid ).header("accessToken", accessToken).get( function (error, data) {
        console.log("test start: " + JSON.stringify(data));
    });
}

function getDAG(tid) {
    d3.json(baseURL + "/rest/1.0/tenant/dag/" + tid ).header("accessToken", accessToken).get( function (error, data) {
        console.log(JSON.stringify(data));

        currtid = tid;
        dag(data, tid);
        if (!connected) {
            connect();
            console.log("connected to WS");
        }

    });
}

function connect() {
    wsocketSync = new WebSocket("ws://" + serviceHost + ":" + servicePort + "/ws/tenants");    //relative reference?
    wsocketSync.onmessage = onMessage;
    connected = true;
}

function dbl(d) {
    alert(JSON.stringify(d));
}

function onMessage(evt) {
    onEvent(evt);

}

function onEvent(evt) {

    console.log(evt.data);



    update(JSON.parse(evt.data));


}


function update(data)
{
    var nodes = force.nodes();

    nodes.forEach(function (d) {

        data.nodes.forEach(function (d2) {
            if (d.id == d2.id) {
                d.details = d2.details;
            }
        });
    });

    draw(nodes, false);
}

function draw(nodes, redraw) {

    //arrow

    if (!svg.select("defs")) {
        svg.apppend("defs");
    }

    svg.select("defs").selectAll("marker")
        .data(["end"])
        .enter().append("marker")
        .attr("id", function(d) { return d; })
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 25)
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5 L10,0 L0, -5")
        .style("stroke", "#4679BD")
        .style("opacity", "0.6");

     link = svg.selectAll(".link")
        .data(force.links())
        .enter().append("line")
        .attr("class", "link")
        .style("stroke-width", function (d) {
            //return Math.sqrt(d.value);
            return 1;
        }).style("marker-end",  "url(#end)");


     var n =  svg.selectAll("g.node")
        .data(nodes, function(d) {
            return d.id;
        });

     var nEnter = n
        .enter().append("g")
        .attr("class", "node")
        .call(force.drag).on("dblclick", dbl);




    // Transition nodes to their new position.
   /* var nodeUpdate = node.transition()
        .duration(200)
        .attr("transform", function(d) {
            return "translate(" + d.y + "," + d.x + ")";
        });*/


 if (redraw) {
     nEnter.filter(function (d) {
         return (d.type == "topic");
     }).append("text")
         .attr("x", -4)
         .attr("id", function (d) {
             return d.id;
         })
         .attr("dy", ".35em")
         .text(function (d) {
             return d.name;
         });

     nEnter.filter(function (d) {
         return (d.type == "deadend");
     }).append("text")
         .attr("x", 14)
         .attr("id", function (d) {
             return d.id;
         })
         .attr("dy", ".35em")
         .text(function (d) {
             return d.name;
         });

     nEnter.filter(function (d) {
         return (d.type == "processor");
     }).append("text")
         .attr("x", -4)
         .attr("id", function (d) {
             return d.id;
         })
         .attr("dy", ".35em")
         .text(function (d) {
             return d.name;
         });

     nEnter.filter(function (d) {
         return (d.type == "topic");
     }).append("rect")
         .attr('x', function (d) {
             return -8;
         })
         .attr('y', function (d) {
             return -10;
         })
         //.attr("rx", function (d) { return 10; }).attr("ry", function (d) { return 10; })
         .attr('height', function (d) {
             return 20;
         })
         .attr('width', function (d) {

             var el = document.getElementById(d.id);
             if (el == null) {
                 console.log(d.id);
             }
             var dim = el.getBBox();
             d.x2 = dim.width + 8
             d.y2 = d.y + 24;
             return d.x2;

         });

     nEnter.filter(function (d) {  return (d.type == "deadend");})
      .append("circle")
      .attr("fill", "black")
      .attr("r", function(d) {
      d.x2 = d.x;
      return 10;
      });


        nEnter.filter(function (d) {  return (d.type == "processor");}).append("rect")
      .attr('x', function (d) { return -8; })
      .attr('y', function (d) { return -10; })
      .attr("rx", function (d) { return 10; }).attr("ry", function (d) { return 10; })
      .attr('height', function (d) { return 20; })
      .attr('width', function (d) {

      var el = document.getElementById(d.id);
      if (el == null) {
      console.log(d.id);
      }
      var dim = el.getBBox();
      d.x2 = dim.width + 8;
      d.y2 = d.y + 28;
      return d.x2;
      });


     nEnter.filter(function (d) {  return (d.type == "processor");})
         .append("circle")
         .attr("r", function(d) {
             d.x2 = d.x;
             return 10;
         })
         .attr("fill",function(d) {
             if (!d.details) {
                 return "yellow";
             }

             if (d.details.state == "PROCESSING") {
                 return "limegreen";
             }
             if (d.details.state == "READY") {
                 return "lightblue";
             }
             if (d.details.state == "ERROR") {
                 return "orange";
             }
             if (d.details.state == "UNKNOWN") {
                 return "yellow";
             }


         })
         .attr("cx" , function(d) {
             var el = document.getElementById(d.id);
             if (el == null) {
                 console.log(d.id);
             }
             var dim = el.getBBox();
             return dim.width + 12;

         }).append("g").attr("width", "50px").attr("height", "20px")
         .attr("class", "offset").append("text").attr("id", function(d) {return d.id + "_2";});


 }


//plot processor badges
  stuff(n);

 node = n;


}


function stuff (nodies) {
    nodies.filter(function (d) {  return (d.type == "processor");})
        .select("circle")
        .attr("fill",function(d) {
            if (!d.details) {
                return "yellow";
            }

            if (d.details.state == "PROCESSING") {
                return "limegreen";
            }
            if (d.details.state == "READY") {
                return "lightblue";
            }
            if (d.details.state == "ERROR") {
                return "orange";
            }
            if (d.details.state == "UNKNOWN") {
                return "yellow";
            }


        });


     var texts =  nodies.filter(function (d) {  return (d.type == "processor");})
        .select("g.badge").select("text");

        texts.attr("x",function (d) {

                return 888;
              /*var el = document.getElementById(d.id);
              if (el == null) {
                  console.log(d.id);
              }
              var dim = el.getBBox();
              return dim.width + 10;*/
          }
      )
        .attr("dy", ".35em");



       texts.text(function(d) {
            if (d.details) {
               // console.log(JSON.stringify(d));
                if (d.details.offset_status) {
                    if ( d.details.offset_status.length > 0) {
                        return d.details.offset_status[0].lastOffset;
                    } else {return "0";}
                }
                else {return "0";}
            }
            else {return "0";}
        })
    ;


}

function dag(data, tid) {

    var width = 1400,
        height = 740
    //fix bundle topic and deadends to bound the diagram
    //todo: make more generic -- i.e. identify "key" or "entry point" topics
    data.nodes.forEach(function (d) {
        if (d.type == "topic" && d.name.indexOf("_bundle") != -1) {
            d.fixed = true;
            d.x = 20;
            d.y = 350;
        }

        if (d.name == "raw_bundle_proc") {
            d.fixed = true;
            d.x = 180;
            d.y = 180;
        }


        if (d.name == "hdfs") {
            d.fixed = true;
            d.x = 1240;
            d.y = 300;
        }
        if (d.name == "elastic_search") {
            d.fixed = true;
            d.x = 1240;
            d.y = 500;
        }
    });


    //if (doForce) {

        force = d3.layout.force()
            .nodes(d3.values(data.nodes))
            .links(data.links)
            .size([width, height])
            .linkDistance(80)
            .charge(function(d) {
                if (d.type === 'deadend')  return -200;
                if (d.type === 'topic')  return -2400;
                return -4600;
            })
            .on("tick", tick);

        force.start();

    //}



    function overlap(a,b) {
        if (a.type == "processor" && b.type == "processor") {
            var x = ( (b.y > a.y && b.y < a.y2) || (b.y2 > a.y  && b.y2 < a.y2 ));
            return x;
        }
        if (a.type == "topic" && b.type == "topic") {
            var x = ( (b.y > a.y && b.y < a.y2) || (b.y2 > a.y && b.y2 < a.y2));
            return x;
        }
        else {return false;}

    }

    var padding = 1, // separation between circles
        radius=8;
    function collide(node) {
        var quadtree = d3.geom.quadtree(data.nodes);

        var nx1, nx2, ny1, ny2, padding;
        padding = 60;
        nx1 = node.x - padding;
        nx2 = node.x2 + padding;
        ny1 = node.y - padding;
        ny2 = node.y2 + padding;
        quadtree.visit(function(quad, x1, y1, x2, y2) {
            //console.log("visiting");
            var dx, dy;
            if (quad.point && (quad.point !== node)) {
                if (overlap(node, quad.point)) {
                    //console.log("overlap" + quad.point.id + node.id);
                    //   dx = Math.min(node.x2 - quad.point.x, quad.point.x2 - node.x) / 2;
                    //  node.x -= dx;
                    //   quad.point.x -= dx;
                    dy = Math.min(node.y2 - quad.point.y, quad.point.y2 - node.y) / 2 ;
                    //dy = 5;
                    node.y -= dy ;
                    node.y2 = node.y + 24;

                    quad.point.y += dy ;
                    quad.point.y2 =  quad.point.y + 24;
                }
            }
            return  y1 > ny2 || y2 < ny1;
        });
    }







    var xx = d3.selectAll("svg").remove();

    svg = d3.select("body").append("svg")
        .attr("width", width)
        .attr("height", height);

    svg
        .append("rect")
        .attr('x', function (d) { return 8; })
        .attr('y', function (d) { return 10; })
        //.attr("rx", function (d) { return 10; }).attr("ry", function (d) { return 10; })
        .attr('height', function (d) { return 20; })
        .attr('width', function (d) { return 40;
        })
        .on("click", function(tid){
            startTest(tid);
        });

    svg
        .append("rect")
        .attr('x', function (d) { return 68; })
        .attr('y', function (d) { return 10; })
        .attr('fill', 'yellow')
        //.attr("rx", function (d) { return 10; }).attr("ry", function (d) { return 10; })
        .attr('height', function (d) { return 20; })
        .attr('width', function (d) { return 50;
        })
        .on("click", function(tid){
            stopTest(tid);
        });

  //fill out the force layout with the design
   draw(force.nodes(), true);

    function tick() {
        link.attr("x1", function (d) {
            var el = document.getElementById(d.source.id);

            if (el == null) {
                d.source.x;
            }
            else {
                var dim = el.getBBox();
                return d.source.x + dim.width;
            }

        })
            .attr("y1", function (d) {return d.source.y;})
            .attr("x2", function (d) {return d.target.x;})
            .attr("y2", function (d) {return d.target.y;});

        node.attr("cx", function (d) {return d.x;})
            .attr("cy", function (d) {return d.y;
            });

        node.attr("transform", function(d) {
            d.y2 = d.y + 24;
            return "translate(" + d.x + "," + d.y + ")"; });
        node.each(function(d) {
            collide(d);
        });

    }

}




$.ajax
({
    type: "POST",
    url: '/rest/1.0/Account/Login',
    contentType: "application/json",
    dataType: "json",
    data: JSON.stringify({ "tenantKey" : "0", "userId" : "admin", "userName" : "admin", "password" : "admin" }),
    success: function (data) {
        accessToken = data.accessToken;
        d3.json("/rest/1.0/admin/meta").header("accessToken", accessToken).get( function(error, data) {
        console.log(data);
        baseURL = data['base_url'];
        serviceHost = data['service_host'];
        servicePort = data['service_port'];


     //   var baseURL = "http://localhost";
      //  var serviceHost = "http://localhost";
      //  var servicePort = "8080";

        var wsocketSync;

        var currStatus;

        var treeData;

        var tree;

        //connect();

        start();

        function start() {
            //getDAG("1");
           getTenants();

        }

            function getTenants() {
                d3.json(baseURL + "/rest/1.0/tenant/" ).header("accessToken", accessToken).get( function (error, data) {
                    console.log(JSON.stringify(data));

                    tabulate(data, ["tid", "ln"]);
                });
            }


        function tabulate(data, columns) {
            var table = d3.select("#tenants").append("table")
                    .attr("style", "margin-left: 10px;" )
                    .attr("class", "table" );
                thead = table.append("thead"),
                tbody = table.append("tbody");

            // append the header row
            thead.append("tr")
                .selectAll("th")
                .data(columns)
                .enter()
                .append("th")
                .text(function(column) { return column; });

            // create a row for each object in the data
            var rows = tbody.selectAll("tr")
                .data(data)
                .enter()
                .append("tr");

            // create a cell in each row for each column
            var cells = rows.selectAll("td")
                .data(function(row) {
                    return columns.map(function(column) {
                        return {column: column, value: row[column]};
                    });
                })
                .enter()
                .append("td")
                .attr("style", "font-family: Courier;")
                .html(function(d) {
                    if (d.column == "tid") {
                        var a = "<a onclick='' href='javascript:getDAG(\"" + d.value + "\");'> " + d.value +  "</a>";
                        console.log(a);
                        return a;
                    }
                    else {return d.value;}
                });

            return table;
        }




    });






    }


})
