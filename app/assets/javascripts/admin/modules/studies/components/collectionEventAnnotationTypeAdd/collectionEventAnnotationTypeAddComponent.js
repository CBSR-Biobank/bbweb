/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./collectionEventAnnotationTypeAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function Controller($controller,
                    $state,
                    notificationsService,
                    domainNotificationService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.domainObjTypeName = 'Collection Event Type';
    vm.addAnnotationTypePromiseFunc = vm.collectionEventType.addAnnotationType.bind(vm.collectionEventType);
    vm.returnState = 'home.admin.studies.study.collection.ceventType';

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

export default ngModule => ngModule.component('collectionEventAnnotationTypeAdd', component)
