/**
 * AngularJS Component for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.components.centreLocationAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function CentreLocationAddController($state,
                                     gettextCatalog,
                                     domainNotificationService,
                                     notificationsService,
                                     modalService) {
  var vm = this;
  vm.$onInit = onInit;

  //---
  function onInit() {
    vm.submit = submit;
    vm.cancel = cancel;
    vm.returnStateName = 'home.admin.centres.centre.locations';
  }

  function submit(location) {
    const submitError = error => {
      if (error.message.startsWith('EntityCriteriaError: location name already used:')) {
        modalService.modalOk(
          gettextCatalog.getString('Location name error'),
          gettextCatalog.getString('The location name <b>{{name}}</b> is already in use for this centre',
                                   { name: location.name }))
        return;
      }

      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('location'));
    }

    vm.centre.addLocation(location)
      .then(submitSuccess)
      .catch(submitError);

    //--

    function submitSuccess() {
      notificationsService.submitSuccess();
      $state.go(vm.returnStateName, {}, { reload: true });
    }
  }

  function cancel() {
    $state.go(vm.returnStateName);
  }

}

/**
 * An AngularJS component that allows the user to add a {@link domain.Location Location} to a {@link
 * domain.centres.Centre Centre}.
 *
 * @memberOf admin.centres.components.centreLocationAdd
 *
 * @param {domain.centres.Centre} centre - the centre to add the location to.
 */
const centreLocationAddComponent = {
  template: require('./centreLocationAdd.html'),
  controller: CentreLocationAddController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('centreLocationAdd', centreLocationAddComponent)
