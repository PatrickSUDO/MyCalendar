$(function() {



    $("#SendButton" ).click(function($e) {

        var event = $('#event').val();
        var event_date = $('#event_date').val();
        var description = $('#description').val();
        var status = $('#status').val();


        if (event.length > 100)
        {
            alert("Event has too many characters");
            return;
        }


        if (new Date(event_date) == "Invalid Date")
        {
            alert("Date should be in the format YYYY-MM-DD");
            return;
        }


        if (description.length > 350)
        {
            alert("Description has too many characters");
            return;
        }

        //invokes the getMyForms POST operation
        var xhr = new XMLHttpRequest();
        xhr.addEventListener("load", loadNewItems, false);
        xhr.open("POST", "../add", true);   //buildFormit -- a Spring MVC controller
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");//necessary
        xhr.send("event=" + event + "&event_date=" + event_date+ "&description=" + description);
    } );// END of the Send button click

    //Handler for the uploadSave call
    //This will populate the Data Table widget
    function loadNewItems(event) {

        var msg = event.target.responseText;
        alert("You have successfully added item "+msg)

    }

} );



function getDataValue()
{
    var radioValue = $("input[name='optradio']:checked").val();
    return radioValue;
}