/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/nameAndStateFilters/nameAndStateFilters.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      stateData:            '<',
      onNameFilterUpdated:  '&',
      onStateFilterUpdated: '&',
      onFiltersCleared:     '&'
    }
  };

  Controller.$inject = ['$controller'];

  /*
   * Controller for this component.
   */
  function Controller($controller) {
    var vm = this;

    // initialize this controller's base class
    $controller('NameAndStateFiltersController', { vm: vm });
  }

  return component;
});
