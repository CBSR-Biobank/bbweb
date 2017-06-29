/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /*
   * This component lists the collection events for a participant.
   */
  var component = {
    templateUrl: '/assets/javascripts/collection/components/ceventsList/ceventsList.html',
    controller: CeventsListController,
    controllerAs: 'vm',
    bindings: {
      participant: '=',
      collectionEventTypes: '='
    }
  };

  //CeventsListController.$inject = [];

  /*
   * Controller for this component.
   */
  function CeventsListController() {
    var vm = this;

    if (vm.collectionEventTypes.length <= 0) {
      throw new Error('no collection event types defined for this study');
    }
  }

  return component;
});
