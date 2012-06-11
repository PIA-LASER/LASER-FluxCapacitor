var mapredUpdateFailed = false;
var dfsUpdateFailed = false;

var updateClusterStatus = function () {


    if (mapredUpdateFailed) {
        $(".clusterValue").remove();
        $('#totalMap').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
        $('#usedMap').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
        $('#totalReduce').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
        $('#usedReduce').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
        $('#totalActiveTrackers').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
        $('#curActiveTrackers').append('<td class="clusterValue"><span class="label label-important">connection failed</span></td>');
    }

    $.getJSON('laser/clusterstatus',function (data) {
        mapredUpdateFailed = false;
        $(".clusterValue").remove();
        $('#totalMap').append('<td class="clusterValue">' + data['max_map_tasks'] + '</td>');
        $('#usedMap').append('<td class="clusterValue">' + data['map_tasks'] + '</td>');
        $('#totalReduce').append('<td class="clusterValue">' + data['max_reduce_tasks'] + '</td>');
        $('#usedReduce').append('<td class="clusterValue">' + data['reduce_tasks'] + '</td>');
        $('#totalActiveTrackers').append('<td class="clusterValue">' + data['numActiveTrackers'] + '</td>');
        $('#curActiveTrackers').append('<td class="clusterValue">' + data['numActiveTrackers'] + '</td>');
    }).error(function () {
            mapredUpdateFailed = true;
        });


    if(dfsUpdateFailed) {
        $(".dataValue").remove();
        $('#totalInput').append('<td class="dataValue"><span class="label label-important">connection failed</span></td>');
        $('#totalOutput').append('<td class="dataValue"><span class="label label-important">connection failed</span></td>');
    }
    $.getJSON('laser/datastatus', function (data) {
        dfsUpdateFailed = false;
        $(".dataValue").remove();
        $('#totalInput').append('<td class="dataValue">' + data['inputSize'] + ' bytes</td>');
        $('#totalOutput').append('<td class="dataValue">' + data['outputSize'] + ' bytes</td>');
    }).error(function () {
            dfsUpdateFailed = true;
        });
}

$(document).ready(function () {
    updateClusterStatus();
    window.setInterval(function () {
        updateClusterStatus();
    }, 5000);

    $("#startRecommendationBtn").click(function () {
        $("#jobConfigForm").submit();
    });
});
