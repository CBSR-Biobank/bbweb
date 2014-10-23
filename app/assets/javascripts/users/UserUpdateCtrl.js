define(['./module'], function(module) {
  'use strict';

  /**
   * Displays a list of users in a table.
   */
  module.controller('UserUpdateCtrl', UserUpdateCtrl);

  UserUpdateCtrl.$inject = [
    '$state', '$filter', 'domainEntityUpdateError', 'usersService', 'modalService', 'stateHelper', 'user'
  ];

  function UserUpdateCtrl($state, $filter, domainEntityUpdateError, usersService, modalService, stateHelper, user) {
    var vm = this;
    var returnState = $state.current.data.returnState;

    vm.user = user;
    vm.password = '';
    vm.confirmPassword = '';
    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      stateHelper.reloadStateAndReinit(returnState);
    }

    function submit(user, password) {
      usersService.update(user, password).then(
        gotoReturnState,
        function(error) {
          domainEntityUpdateError(error, 'user', returnState);
        });
    }

    function cancel() {
      gotoReturnState();
    }

  }

});
