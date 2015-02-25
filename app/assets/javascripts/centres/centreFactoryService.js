define(['./module'], function(module) {
  'use strict';

  module.service('centreFactory', centreFactoryService);

  centreFactoryService.$inject = [
    'Centre',
    'Location',
    'centresService',
    'centreLocationsSevice'
  ];

  /**
   *
   */
  function centreFactory(Centre,
                         Location,
                         centresService,
                         centreLocationsSevice) {
    var service = {
      getCentres:      getCentres,
      get:             get
    };
    return service;

    //-------

    function getCentres(options) {
      return centresService.getCentres(options).then.(function(reply) {
        return _.map(reply, function(obj){
          return new Centre(obj);
        });
      });
    }

    function getCentre(id) {
      return centresService.get(id).then(function(reply) {
        return new Centre(reply);
      });
    }
  }

});
