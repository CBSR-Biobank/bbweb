define(['./module'], function(module) {
  'use strict';

  module.controller('CentreEditCtrl', CentreEditCtrl);

  CentreEditCtrl.$inject = [
    '$scope', '$state', 'stateHelper', 'centreService', 'domainEntityUpdateError', 'user', 'centre',
  ];

  /**
   *
   */
  function CentreEditCtrl($scope, $state, stateHelper, centreService, domainEntityUpdateError, user, centre) {
    var vm = this;
    var action = centre.id ? 'Update' : 'Add';
    var returnState = $state.current.data.returnState;

    vm.title = action + ' centre';
    vm.centre = centre;
    vm.submit = submit;
    vm.cancel = cancel;

    //---

    function gotoReturnState() {
      stateHelper.reloadStateAndReinit(returnState);
    }

    function submit(centre) {
      centreService.addOrUpdate(centre).then(
        gotoReturnState,
        function(error) {
          domainEntityUpdateError(error, 'centre', returnState);
        });
    }

    function cancel() {
      gotoReturnState();
    }
  }

});
