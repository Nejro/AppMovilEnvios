package com.under.tptr_client.model

class PackageClient() {
    var guia:String = ""
    var idCliente:String = ""
    var direccion:String = ""
    var estado:String = ""
    var latitud:Double = 0.0
    var longitud:Double = 0.0
    val DISTRIBUTION_STATE:String = "DISTRIBUCION"
    val DELIVERED_STATE:String = "ENTREGADO"
    val WAREHOUSE_STATE:String = "BODEGA"
}