package models

data class TaskModel(
    var id:String = "",
    var name:String = "",
    var details:String = "",
    var dueDate:String = "",
    var creationDate:String = "",
    var state:String = "",
    var status:String = "",
    var category:String = "",
    var outcome:String = "",
    var inTrash:Boolean = false
)