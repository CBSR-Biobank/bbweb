/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionEventAnnotationTypeAddDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:               '=',
        collectionEventType: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/annotationTypes/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAdd.html',
      controller: CollectionEventAnnotationTypeAddCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionEventAnnotationTypeAddCtrl.$inject = [
    'annotationTypeAddMixin'
  ];

  var returnState = 'home.admin.studies.study.collection.ceventType';

  function CollectionEventAnnotationTypeAddCtrl(annotationTypeAddMixin) {
    var vm = this;

    _.extend(vm, annotationTypeAddMixin);

    vm.onSubmit        = onSubmit;
    vm.onAddsuccessful = vm.onAddSuccessful(returnState);
    vm.onCancel        = vm.onCancel(returnState);

    //--

    function onSubmit(annotationType) {
      vm.collectionEventType.addAnnotationType(annotationType)
        .then(vm.onAddsuccessful)
        .catch(vm.onAddFailed);
    }
  }

  return collectionEventAnnotationTypeAddDirective;

});
