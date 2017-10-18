/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./participantAnnotationTypeAdd.html'),
  controller: ParticipantAnnotationTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantAnnotationTypeAddController($controller,
                                                $state,
                                                notificationsService,
                                                domainNotificationService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.domainObjTypeName = 'Study';
    vm.addAnnotationTypePromiseFunc = vm.study.addAnnotationType.bind(vm.study);
    vm.returnState = 'home.admin.studies.study.participants';

    // initialize this controller's base class
    $controller('AnnotationTypeAddController', {
      vm:                        vm,
      $state:                    $state,
      notificationsService:      notificationsService,
      domainNotificationService: domainNotificationService
    });

    vm.init();
  }

}

export default ngModule => ngModule.component('participantAnnotationTypeAdd', component)
