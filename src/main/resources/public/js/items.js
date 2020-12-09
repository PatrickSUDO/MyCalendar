$(function() {

    $('input[name="datetimes"]').daterangepicker({
        timePicker: true,
        showDropdowns: true,
        timePicker24Hour: true,
        // startDate: $(this).val().substring(0, 17),
        // endDate: $(this).val().substring(21),
        autoUpdateInput: true,
        locale:{
            format: 'YYYY-MM-DD HH:mm'
        }
    });
    $('input[name="datetimes"]').on('apply.daterangepicker', function(ev, picker) {
        $(this).val(picker.startDate.format('YYYY-MM-DD HH:mm') + ' - ' + picker.endDate.format('YYYY-MM-DD HH:mm'));
    });

});

$(function() {

    $( "#dialogtemplate2" ).dialog();

    $('#myTable').DataTable( {
        scrollY:        "500px",
        scrollX:        true,
        scrollCollapse: true,
        paging:         true,
        columnDefs: [
            { width: 200, targets: 0 }
        ],
        fixedColumns: true
    } );

    var table = $('#myTable').DataTable();
    $('#myTable tbody').on( 'click', 'tr', function () {
        if ( $(this).hasClass('selected') ) {
            $(this).removeClass('selected');
        }
        else {
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
        }
    } );
});


function modItem()
{
    var id = $('#id').val();
    var event = $('#event').val();
    var event_date = $('#event_date').val();
    var description = $('#description').val();

    if (id == "")
    {
        alert("Please select an item from the table");
        return;
    }

    if (event.length > 100)
    {
        alert("Event has too many characters");
        return;
    }


    if (description.length > 350)
    {
        alert("Description has too many characters");
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.addEventListener("load", loadMods, false);
    xhr.open("POST", "../changewi", true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
    xhr.send("id=" + id + "&event=" + event + "&event_date=" + event_date + "&description=" + description);
}

function loadMods(event) {

    var msg = event.target.responseText;
    alert("You have successfully modified item "+msg)

    $('#id').val("");
    $('#event').val("");
    $('#event_date').val("");
    $('#description').val("");

    //Refresh the grid
    GetItems();

}


// Populate the table with work items
function GetItems() {
    var xhr = new XMLHttpRequest();
    var type="active";
    xhr.addEventListener("load", loadItems, false);
    xhr.open("POST", "../retrieve", true);   //buildFormit -- a Spring MVC controller
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
    xhr.send("type=" + type);
}



function loadItems(event) {

    $("#modform").show();

    var xml = event.target.responseText;
    var oTable = $('#myTable').dataTable();
    oTable.fnClearTable(true);

    $(xml).find('Item').each(function () {

        var $field = $(this);
        var id = $field.find('Id').text();
        var name = $field.find('Name').text();
        var event = $field.find('Event').text();
        var event_date = $field.find('EventDate').text();
        var description = $field.find('Description').text();
        var date = $field.find('Date').text();

        //Set the new data
        oTable.fnAddData( [
            id,
            name,
            event,
            event_date,
            description,
            date,]
        );
    });


}

function ModifyItem() {
    var table = $('#myTable').DataTable();
    var myId="";
    var arr = [];
    $.each(table.rows('.selected').data(), function() {

        var value = this[0];
        myId = value;
    });

    if (myId == "")
    {
        alert("You need to select a row");
        return;
    }

    // Post to modify
    var xhr = new XMLHttpRequest();
    xhr.addEventListener("load", onModifyLoad, false);
    xhr.open("POST", "../modify", true);   //buildFormit -- a Spring MVC controller
    xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");//necessary
    xhr.send("id=" + myId);
}


// Handler for the uploadSave call
function onModifyLoad(modifyEvent) {

    var xml = modifyEvent.target.responseText;
    $(xml).find('Item').each(function () {

        var $field = $(this);
        var id = $field.find('Id').text();
        var event = $field.find('Event').text();
        var event_date = $field.find('EventDate').text();
        var description = $field.find('Description').text();
        var date = $field.find('Date').text();

        //Set the fields
        $('#id').val(id);
        $('#event').val(event);
        $('#event_date').val(event_date);
        $('#description').val(description);
        $('#date').val(date);

    });
}

function DeleteItem() {
    var table = $('#myTable').DataTable();
    var myId="";
    var arr = [];
    $.each(table.rows('.selected').data(), function() {

        var value = this[0];
        myId = value;
    });

    if (myId == "")
    {
        alert("You need to select a row");
        return;
    }

    // Post to modify
    var xhr = new XMLHttpRequest();
    xhr.addEventListener("load", onDeleteLoad, false);
    xhr.open("DELETE", "../delete", true);   //buildFormit -- a Spring MVC controller
    xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");//necessary
    xhr.send("id=" + myId);
}

// Handler for the delete call
function onDeleteLoad(deleteEvent) {

    var msg = deleteEvent.target.responseText;
    alert("You have successfully deleted item "+msg)

    //Refresh the grid
    GetItems();
}



function Share() {
    var email = $("input[name='mail']").val();

    //Valid the email
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    if (!re.test(String(email).toLowerCase())){
        alert("Please enter correct email.")
        return;
    }
    // Post to report
    var xhr = new XMLHttpRequest();
    xhr.addEventListener("load", onShare, false);
    xhr.open("POST", "../report", true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
    xhr.send("email=" + email);
}

function onShare(reportEvent) {var data = reportEvent.target.responseText;
    alert(data);
}




