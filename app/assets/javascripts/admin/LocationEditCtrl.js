define(['./module'], function(module) {
  'use strict';

  module.controller('LocationEditCtrl', LocationEditCtrl);

  LocationEditCtrl.$inject = [
    '$state',
    'domainEntityUpdateError',
    'centreLocationService',
    'centre',
    'location'
  ];

  /**
   *
   */
  function LocationEditCtrl($state,
                            domainEntityUpdateError,
                            centreLocationService,
                            centre,
                            location) {

    var action = location.id ? 'Update' : 'Add';

    var vm = this;
    vm.title =  action + ' Location';
    vm.centre = centre;
    vm.location = location;

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      return $state.go('admin.centres.centre.locations', {}, {reload: true});
    }

    function submit(location) {
      if (location.id) {
        // remove the previous location before adding the new one
        centreLocationService.remove(vm.centre.id, location.id).then(addLocation);
      } else {
        addLocation();
      }

      function addLocation() {
        centreLocationService.add(vm.centre, location)
          .then(gotoReturnState)
          .catch(function(error) {
            domainEntityUpdateError.handleError(
              error,
              'location',
              'admin.centres.centre.locations',
              {},
              {reload: true});
          });
      }
    }

    function cancel() {
      gotoReturnState();
    }
  }

});
