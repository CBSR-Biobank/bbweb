// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: AnnotationTypeEditCtrl', function() {
    var scope,
        state,
        controller,
        rootScope,
        fakeEntities,
        domainEntityService,
        notificationsService,
        ParticipantAnnotationType,
        AnnotationValueType,
        study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($state,
                               $controller,
                               $rootScope,
                               _domainEntityService_,
                               _notificationsService_,
                               Study,
                               _ParticipantAnnotationType_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      state                     = $state;
      controller                = $controller;
      rootScope                 = $rootScope;
      fakeEntities              = fakeDomainEntities;
      domainEntityService   = _domainEntityService_;
      notificationsService      = _notificationsService_;
      ParticipantAnnotationType = _ParticipantAnnotationType_;
      AnnotationValueType       = _AnnotationValueType_;

      study = new Study(fakeEntities.study());

      spyOn(state, 'go');
    }));

    describe('for participant annotation types', function() {
      var context = {};

      beforeEach(inject(function (ParticipantAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType({ required: true }),
            stateName = 'home.admin.studies.study.participants';


        context.state           = { current: { name: stateName } };
        context.returnState     = stateName;
        context.annotTypeNew    = new ParticipantAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotTypeWithId = new ParticipantAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });

    describe('for collection event annotation types', function() {
      var context = {};

      beforeEach(inject(function (CollectionEventAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType();

        context.state           = { current: { name: 'home.admin.studies.study.collection' } };
        context.returnState     = 'home.admin.studies.study.collection';
        context.annotTypeNew    = new CollectionEventAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotTypeWithId = new CollectionEventAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });

    describe('for specimen link annotation types', function() {
      var context = {};

      beforeEach(inject(function (SpecimenLinkAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType({ required: true }),
            stateName = 'home.admin.studies.study.processing';


        context.state           = { current: { name: stateName } };
        context.returnState     = stateName;
        context.annotTypeNew    = new SpecimenLinkAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotTypeWithId = new SpecimenLinkAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });


    function sharedBehaviour(context) {

      describe('(shared)', function() {

        var q, state, returnState, annotTypeNew, annotTypeWithId, ParticipantAnnotationType;

        beforeEach(inject(function ($q, _ParticipantAnnotationType_) {
          q               = $q;
          state           = context.state;
          returnState     = context.returnState;
          annotTypeNew    = context.annotTypeNew;
          annotTypeWithId = context.annotTypeWithId;
          ParticipantAnnotationType = _ParticipantAnnotationType_;

          state.go = function () {};
          spyOn(state, 'go');
        }));

        it('scope should be valid when adding', function() {
          createController(state, annotTypeNew);

          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.annotType).toEqual(annotTypeNew);
          expect(scope.vm.title).toBe('Add Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(annotTypeNew instanceof ParticipantAnnotationType);
          expect(scope.vm.valueTypes).toEqual(AnnotationValueType.values());
        });

        it('scope should be valid when updating', function() {
          createController(state, annotTypeWithId);

          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.annotType).toEqual(annotTypeWithId);
          expect(scope.vm.title).toBe('Update Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(annotTypeWithId instanceof ParticipantAnnotationType);
          expect(scope.vm.valueTypes).toEqual(AnnotationValueType.values());
        });

        it('throws an exception if the current state is invalid', function() {
          var invalidState = { current: { name: 'xyz' } };
          expect(function () {
            createController(invalidState, annotTypeWithId);
          }).toThrow(new Error('invalid current state name: ' + invalidState.current.name));
        });

        it('maxValueCountRequired is valid', function() {
          annotTypeWithId.valueType = AnnotationValueType.SELECT();
          annotTypeWithId.maxValueCount = 0;
          createController(state, annotTypeWithId);

          annotTypeWithId.maxValueCount = 0;
          expect(scope.vm.maxValueCountRequired()).toBe(true);

          annotTypeWithId.maxValueCount = 3;
          expect(scope.vm.maxValueCountRequired()).toBe(true);

          annotTypeWithId.maxValueCount = 1;
          expect(scope.vm.maxValueCountRequired()).toBe(false);

          annotTypeWithId.maxValueCount = 2;
          expect(scope.vm.maxValueCountRequired()).toBe(false);
        });

        it('calling valueTypeChange clears the options array', function() {
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          createController(state, annotTypeWithId);

          scope.vm.valueTypeChange();
          expect(annotTypeWithId.options).toBeArray();
          expect(annotTypeWithId.options).toBeEmptyArray();
        });

        it('calling optionAdd creates the options array', function() {
          createController(state, annotTypeWithId);
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();

          scope.vm.optionAdd();
          expect(annotTypeWithId.options).toBeArrayOfSize(1);
          expect(annotTypeWithId.options).toBeArrayOfStrings();
        });

        it('calling optionAdd appends to the options array', function() {
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();
          createController(state, annotTypeWithId);

          scope.vm.optionAdd();
          expect(annotTypeWithId.options).toBeArrayOfSize(1);
          expect(annotTypeWithId.options).toBeArrayOfStrings();
        });

        it('calling optionRemove throws an error on empty array', function() {
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();
          createController(state, annotTypeWithId);
          expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove throws an error if removal results in empty array', function() {
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();
          annotTypeWithId.options = ['abc'];
          createController(state, annotTypeWithId);
          expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove removes an option', function() {
          // note: more than two strings in options array
          var options = ['abc', 'def'];
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();
          annotTypeWithId.options = options.slice(0);
          createController(state, annotTypeWithId);
          scope.vm.optionRemove('abc');
          expect(annotTypeWithId.options).toBeArrayOfSize(options.length - 1);
        });

        it('calling removeButtonDisabled returns valid results', function() {
          // note: more than two strings in options array
          var options = ['abc', 'def'];
          annotTypeWithId.valueType = 'Select';
          annotTypeWithId.maxValueCount = 1;
          annotTypeWithId.valueTypeChanged();
          annotTypeWithId.options = options.slice(0);
          createController(state, annotTypeWithId);

          expect(scope.vm.removeButtonDisabled()).toEqual(false);

          annotTypeWithId.options = options.slice(1);
          expect(scope.vm.removeButtonDisabled()).toEqual(true);
        });

        it('when adding should return to the valid state on submit', function() {
          onSubmit(state, annotTypeNew, returnState);
        });

        it('when adding should return to the valid state on cancel', function() {
          onCancel(state, annotTypeNew, returnState);
        });

        it('when submitting and server responds with an error, the error message is displayed', function() {
          var domainEntityService = this.$injector.get('domainEntityService');

          spyOn(domainEntityService, 'updateErrorModal')
            .and.callFake(function () {});

          spyOnAnnotTypeAddOrUpdateAndReject(annotTypeNew);

          createController(state, annotTypeNew);
          scope.vm.submit(annotTypeNew);
          scope.$digest();

          expect(domainEntityService.updateErrorModal).toHaveBeenCalled();
        });

        it('when updating should return to the valid state on submit', function() {
          onSubmit(state, annotTypeWithId, returnState);
        });

        it('when updating should return to the valid state on cancel', function() {
          onCancel(state, annotTypeWithId, returnState);
        });

        function createController(state, annotType) {
          scope = rootScope.$new();

          controller('AnnotationTypeEditCtrl as vm', {
            $scope:                    scope,
            $state:                    state,
            notificationsService:      notificationsService,
            domainEntityService:   domainEntityService,
            ParticipantAnnotationType: ParticipantAnnotationType,
            AnnotationValueType:       AnnotationValueType,
            study:                     study,
            annotType:                 annotType
          });
          scope.$digest();
        }

        function spyOnAnnotTypeAddOrUpdateAndResolve(annotType) {
          spyOn(annotType, 'addOrUpdate').and.callFake(function () {
            var deferred = q.defer();
            deferred.resolve('xxx');
            return deferred.promise;
          });
        }

        function spyOnAnnotTypeAddOrUpdateAndReject(annotType) {
          spyOn(annotType, 'addOrUpdate').and.callFake(function () {
            var deferred = q.defer();
            deferred.reject({ data: { message: 'error'} });
            return deferred.promise;
          });
        }

        function onSubmit(state, annotType, returnState) {
          spyOnAnnotTypeAddOrUpdateAndResolve(annotType);

          createController(state, annotType);
          scope.vm.submit(annotType);
          scope.$digest();
          expect(state.go).toHaveBeenCalledWith(
            returnState, {studyId: study.id}, {reload: true});
        }

        function onCancel(state, annotType, returnState) {
          spyOnAnnotTypeAddOrUpdateAndResolve(annotType);

          createController(state, annotType);
          scope.vm.cancel();
          scope.$digest();
          expect(state.go).toHaveBeenCalledWith(
            returnState, {studyId: study.id}, {reload: true});
        }

      });
    }

  });

});
