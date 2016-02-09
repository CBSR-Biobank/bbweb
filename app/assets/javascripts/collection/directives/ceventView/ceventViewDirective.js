/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  /**
   *
   */
  function ceventViewDirective() {
    var directive = {
      restrict: 'EA',
      scope: {},
      bindToController: {
        collectionEvent: '='
      },
      templateUrl : '/assets/javascripts/collection/directives/ceventView/ceventView.html',
      controller: CeventViewCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CeventViewCtrl.$inject = ['timeService'];

  /**
   *
   */
  function CeventViewCtrl(timeService) {
    var vm = this;

    vm.timeCompletedLocal = timeService.timeToDisplayString(vm.collectionEvent.timeCompleted);
  }

  return ceventViewDirective;
});
