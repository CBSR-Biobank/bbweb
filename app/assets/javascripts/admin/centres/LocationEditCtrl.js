/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  LocationEditCtrl.$inject = [
    '$state',
    'Location',
    'domainEntityService',
    'notificationsService',
    'centre'
  ];

  /**
   * @param {Centre} centre - The centre the location belongs to.
   */
  function LocationEditCtrl($state,
                            Location,
                            domainEntityService,
                            notificationsService,
                            centre) {

    var vm = this;

    vm.centre = centre;
    vm.returnStateName = 'home.admin.centres.centre.locations';

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

    function submit(location) {
      vm.centre.addLocation(location)
        .then(submitSuccess)
        .catch(submitError);

      //--

      function submitSuccess() {
        notificationsService.submitSuccess();
        $state.go(vm.returnStateName, {}, {reload: true});
      }

      function submitError(error) {
        return domainEntityService.updateErrorModal(error, 'location');
      }
    }

    function cancel() {
      $state.go(vm.returnStateName, {}, {reload: false});
    }
  }

  return LocationEditCtrl;
});
