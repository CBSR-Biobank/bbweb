/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/shipmentSpecimens/components/specimenTableAction/specimenTableAction.html',
    controller: SpecimenTableActionDirective,
    controllerAs: 'vm',
    bindings: {
      action:            '<',
      onActionSelected:  '&'
    }
  };

  //SpecimenTableActionDirective.$inject = [];

  /*
   * Controller for this component.
   */
  function SpecimenTableActionDirective() {
    var vm = this;
    vm.$onInit = onInit;

    //---
    function onInit() {

    }

  }

  return component;
});
