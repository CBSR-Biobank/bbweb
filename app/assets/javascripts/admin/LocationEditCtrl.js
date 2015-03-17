define(['underscore'], function(_) {
  'use strict';

  LocationEditCtrl.$inject = [
    '$state',
    'Location',
    'domainEntityUpdateError',
    'notificationsService',
    'centre'
  ];

  /**
   * @param {Centre} centre - The centre the location belongs to.
   */
  function LocationEditCtrl($state,
                            Location,
                            domainEntityUpdateError,
                            notificationsService,
                            centre) {

    var vm = this;

    vm.centre = centre;

    if ($state.current.name === 'home.admin.centres.centre.locationAdd') {
      vm.location = new Location();
      vm.title =  'Add Location';
    } else {
      vm.location = _.findWhere(centre.locations, { id: $state.params.locationId });
      vm.title = 'Update Location';
    }

    vm.submit = submit;
    vm.cancel = cancel;

    //--

    function gotoReturnState() {
      return $state.go('home.admin.centres.centre.locations', {}, {reload: true});
    }

    function submitSuccess() {
      notificationsService.submitSuccess();
      gotoReturnState();
    }

    function submit(location) {
      vm.centre.addLocation(location)
        .then(submitSuccess)
        .catch(function(error) {
          domainEntityUpdateError.handleError(
            error,
            'location',
            'home.admin.centres.centre.locations',
            {},
            {reload: true});
        });
    }

    function cancel() {
      gotoReturnState();
    }
  }

  return LocationEditCtrl;
});
