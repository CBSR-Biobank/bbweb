/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.ceventTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function CeventTypeAddController($state,
                                 gettextCatalog,
                                 CollectionEventType,
                                 domainNotificationService,
                                 notificationsService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.ceventType  = new CollectionEventType({}, { study: vm.study });
    vm.returnState = 'home.admin.studies.study.collection';

    vm.title       = gettextCatalog.getString('Add Collection Event');
    vm.submit      = submit;
    vm.cancel      = cancel;
  }

  function submit() {
    vm.ceventType.add().then(submitSuccess).catch(submitError);

    function submitSuccess() {
      notificationsService.submitSuccess();
      return $state.go(vm.returnState, {}, { reload: true });
    }

    function submitError(error) {
      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('collection event type'));
    }
  }

  function cancel() {
    return $state.go(vm.returnState);
  }

}

/**
 * An AngularJS component that allows the user to add a {@link domain.studies.CollectionEventType
 * CollectionEventType} using an HTML form.
 *
 * @memberOf admin.studies.components.ceventTypeAdd
 *
 * @param {domain.studies.Study} study - the study to add the collection event type to.
 */
const ceventTypeAddComponent = {
  template: require('./ceventTypeAdd.html'),
  controller: CeventTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('ceventTypeAdd', ceventTypeAddComponent)
