/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/nameFilter/nameFilter.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      onNameFilterUpdated:  '&',
      onFiltersCleared:     '&'
    }
  };

  Controller.$inject = ['$controller'];

  /*
   * Controller for this component.
   */
  function Controller($controller) {
    var vm = this;
    vm.$onInit = onInit;

    function onInit() {
      // initialize this controller's base class
      $controller('NameAndStateFiltersController', { vm: vm });
    }
  }

  return component;
});
