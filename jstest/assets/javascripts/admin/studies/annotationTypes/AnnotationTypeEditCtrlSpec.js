// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: AnnotationTypeEditCtrl', function() {
    var scope, stateHelper, domainEntityUpdateError, addOrUpdate;
    var study = {id: 'dummy-study-id', name: 'ST1'};
    var valueTypes = ['Text', 'Select'];

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_stateHelper_, _domainEntityUpdateError_) {
      stateHelper = _stateHelper_;
      domainEntityUpdateError = _domainEntityUpdateError_;

      spyOn(stateHelper, 'reloadStateAndReinit');
      spyOn(domainEntityUpdateError, 'handleError');

      addOrUpdate = jasmine.createSpyObj('addOrUpdate', ['fn']);
    }));

    function createController($controller, $rootScope, state, annotType) {
      scope = $rootScope.$new();
      $controller('AnnotationTypeEditCtrl as vm', {
        $scope:                  scope,
        $state:                  state,
        stateHelper:             stateHelper,
        domainEntityUpdateError: domainEntityUpdateError,
        study:                   study,
        annotType:               annotType,
        addOrUpdateFn:           addOrUpdate.fn,
        valueTypes:              valueTypes
      });
      scope.$digest();
    }

    function onSubmit($q, $controller, $rootScope, state, annotType, returnState) {
      addOrUpdate.fn.and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('xxx');
        return deferred.promise;
      });

      createController($controller, $rootScope, state, annotType);
      scope.vm.submit(annotType);
      scope.$digest();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        returnState, {studyId: study.id}, {reload: true});
    }

    function onCancel($q, $controller, $rootScope, state, annotType, returnState) {
      createController($controller, $rootScope, state, annotType);
      scope.vm.cancel();
      scope.$digest();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        returnState, {studyId: study.id}, {reload: true});
    }

    describe('scope', function() {
      var state = {
        current: {
          name: 'home.admin.studies.study.collection'
        }
      };

      describe('when adding', function() {
        var annotType = {name: 'AT1'};

        it('should be valid when adding', inject(function($controller, $rootScope) {
          createController($controller, $rootScope, state, annotType);
          expect(scope.vm.study).toBe(study);
          expect(scope.vm.annotType).toBe(annotType);
          expect(scope.vm.title).toBe('Add Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(false);
          expect(scope.vm.returnStateParams).toEqual({studyId: study.id});
        }));

      });

      describe('when updating', function() {
        var annotType = {id: 'dummy-annotation-type-di', name: 'AT1'};

        it('should be valid when adding', inject(function($controller, $rootScope) {
          createController($controller, $rootScope, state, annotType);
          expect(scope.vm.study).toBe(study);
          expect(scope.vm.annotType).toBe(annotType);
          expect(scope.vm.title).toBe('Update Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(false);
          expect(scope.vm.returnStateParams).toEqual({studyId: study.id});
        }));

      });

    });

    describe('form callbacks:', function() {
      var state = {
        current: {
          name: 'home.admin.studies.study.collection'
        }
      };
      var annotType = {name: 'AT1'};

      it('maxValueCountRequired is valid', inject(function($controller, $rootScope) {
        createController($controller, $rootScope, state, annotType);

        annotType.maxValueCount = 0;
        expect(scope.vm.maxValueCountRequired()).toBe(true);

        annotType.maxValueCount = 3;
        expect(scope.vm.maxValueCountRequired()).toBe(true);

        annotType.maxValueCount = 1;
        expect(scope.vm.maxValueCountRequired()).toBe(false);

        annotType.maxValueCount = 2;
        expect(scope.vm.maxValueCountRequired()).toBe(false);
      }));

      it('calling valueTypeChange adds an option', inject(function($controller, $rootScope) {
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        createController($controller, $rootScope, state, annotType);

        scope.vm.valueTypeChange();
        expect(annotType.options).toBeArray();
        expect(annotType.options).toBeNonEmptyArray();
        expect(annotType.options).toBeArrayOfStrings();
      }));

      it('calling optionAdd creates the options array', inject(function($controller, $rootScope) {
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = undefined;
        createController($controller, $rootScope, state, annotType);

        scope.vm.optionAdd();
        expect(annotType.options).toBeArrayOfSize(1);
        expect(annotType.options).toBeArrayOfStrings();
      }));

      it('calling optionAdd appends to the options array', inject(function($controller, $rootScope) {
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = [];
        createController($controller, $rootScope, state, annotType);

        scope.vm.optionAdd();
        expect(annotType.options).toBeArrayOfSize(1);
        expect(annotType.options).toBeArrayOfStrings();
      }));

      it('calling optionRemove throws an error on empty array', inject(function($controller, $rootScope) {
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = [];
        createController($controller, $rootScope, state, annotType);
        expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
      }));

      it('calling optionRemove throws an error if removal results in empty array', inject(function($controller, $rootScope) {
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = ['abc'];
        createController($controller, $rootScope, state, annotType);
        expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
      }));

      it('calling optionRemove removes an option', inject(function($controller, $rootScope) {
        // note: more than two strings in options array
        var options = ['abc', 'def'];
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = options.slice(0);
        createController($controller, $rootScope, state, annotType);
        scope.vm.optionRemove('abc');
        expect(annotType.options).toBeArrayOfSize(options.length - 1);
      }));

      it('calling removeButtonDisabled returns valid results', inject(function($controller, $rootScope) {
        // note: more than two strings in options array
        var options = ['abc', 'def'];
        annotType.valueType = 'Select';
        annotType.maxValueCount = 1;
        annotType.options = options.slice(0);
        createController($controller, $rootScope, state, annotType);

        expect(scope.vm.removeButtonDisabled()).toEqual(false);

        annotType.options = options.slice(1);
        expect(scope.vm.removeButtonDisabled()).toEqual(true);
      }));

    });

    describe('for participant annotation types', function() {
      var state = {
        current: {
          name: 'home.admin.studies.study.participants'
        }
      };
      var returnState = 'home.admin.studies.study.participants';

      describe('when adding', function() {
        var annotType = {name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

      describe('when update', function() {
        var annotType = {id: 'dummy-annotation-type-di', name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

    });

    describe('for collection event annotation types', function() {
      var state = {
        current: {
          name: 'home.admin.studies.study.collection'
        }
      };
      var returnState = 'home.admin.studies.study.collection';

      describe('when adding', function() {
        var annotType = {name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

      describe('when updating', function() {
        var annotType = {id: 'dummy-annotation-type-di', name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

    });

    describe('for specimen link annotation types', function() {
      var state = {
        current: {
          name: 'home.admin.studies.study.processing'
        }
      };
      var returnState = 'home.admin.studies.study.processing';

      describe('when adding', function() {
        var annotType = {name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

      describe('when updating', function() {
        var annotType = {id: 'dummy-annotation-type-di', name: 'AT1'};

        it('should return to the valid state on submit', inject(function($q, $controller, $rootScope) {
          onSubmit($q, $controller, $rootScope, state, annotType, returnState);
        }));

        it('should return to the valid state on cancel', inject(function($q, $controller, $rootScope) {
          onCancel($q, $controller, $rootScope, state, annotType, returnState);
        }));

      });

    });

  });

});
