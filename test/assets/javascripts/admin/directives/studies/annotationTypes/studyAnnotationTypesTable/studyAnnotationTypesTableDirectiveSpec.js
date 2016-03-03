/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyAnnotationTypesTableDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function () {
      var self = this;

      self.modalService         = self.$injector.get('modalService');
      self.Study                = self.$injector.get('Study');
      self.AnnotationType       = self.$injector.get('AnnotationType');
      self.AnnotationValueType  = self.$injector.get('AnnotationValueType');
      self.AnnotationTypeViewer = self.$injector.get('AnnotationTypeViewer');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      self.study = new self.Study(self.jsonEntities.study());
      self.annotationTypes = _.map(
        self.AnnotationValueType.values(),
        function(valueType) {
          return new self.AnnotationType(
            self.jsonEntities.annotationType(self.study, { valueType: valueType }));
        });
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.annotationTypeIdsInUse = [ this.annotationTypes[0].uniqueId ];
        context.annotationTypeName   = 'ParticipantAnnotationType';
        context.viewStateName = 'home.admin.studies.study.participants.annotationTypeView';
      });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.annotationTypeIdsInUse = [ this.annotationTypes[0].uniqueId ];
        context.annotationTypeName   = 'CollectionEventAnnotationType';
        context.viewStateName = 'home.admin.studies.study.collection.ceventAnnotationTypeview';
      });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.annotationTypeIdsInUse = [ this.annotationTypes[0].uniqueId ];
        context.annotationTypeName   = 'SpecimenLinkAnnotationType';
        context.viewStateName = 'home.admin.studies.study.processing.spcLinkAnnotationTypeView';
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function($rootScope,
                                   $compile,
                                   testUtils) {

          this.state                    = this.$injector.get('$state');
          this.modalService             = this.$injector.get('modalService');
          this.studyAnnotationTypeUtils = this.$injector.get('studyAnnotationTypeUtils');
          this.AnnotationTypeViewer     = this.$injector.get('AnnotationTypeViewer');

          testUtils.putHtmlTemplates(
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
            '/assets/javascripts/common/directives/updateRemoveButtons.html');

          spyOn(this.state, 'go');

          this.onRemove = jasmine.createSpy('onRemove');

          this.element = angular.element([
            '<study-annotation-types-table',
            '   study="vm.study"',
            '   annotation-types="vm.annotationTypes"',
            '   annotation-type-name="vm.annotationTypeName"',
            '   annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
            '   update-state-name="vm.viewStateName"',
            '   on-remove="vm.onRemove"',
            '</study-annotation-types-table>'
          ].join(''));

          this.scope = $rootScope.$new();
          this.scope.vm = {
            study:                  this.study,
            annotationTypes:        this.annotationTypes,
            annotationTypeIdsInUse: context.annotationTypeIdsInUse,
            annotationTypeName:     context.annotationTypeName,
            viewStateName:          context.viewStateName,
            onRemove:               this.onRemove
          };

          $compile(this.element)(this.scope);
          this.scope.$digest();
          this.controller = this.element.controller('studyAnnotationTypesTable');
        }));

        it('has valid scope', function() {
          expect(this.controller.annotationTypes).toEqual(this.annotationTypes);
          expect(this.controller.annotationTypeIdsInUse).toEqual(context.annotationTypeIdsInUse);
          expect(this.controller.tableParams).not.toBeNull();
          expect(this.controller.modificationsAllowed).toEqual(this.study.isDisabled());
        });

        it('has valid table columns', function() {
          expect(this.controller.columns).toBeArrayOfSize(4);
          expect(this.controller.columns[0].title).toBe('Name');
          expect(this.controller.columns[1].title).toBe('Type');
          expect(this.controller.columns[2].title).toBe('Required');
          expect(this.controller.columns[3].title).toBe('Description');
        });

        it('calling information should open an AnnotationTypeViewer', function() {
          expect(this.controller.information(this.controller.annotationTypes[0]))
            .toEqual(jasmine.any(this.AnnotationTypeViewer));
        });

        it('on update should open a modal to display annotation type in use modal', function() {
          spyOn(this.modalService, 'modalOk');
          this.controller.update(this.controller.annotationTypes[0]);
          expect(this.modalService.modalOk).toHaveBeenCalled();
        });

        it('on update should change state to update an annotation type', function() {
          var annotationTypeToUpdate = this.annotationTypes[1];
          spyOn(this.modalService, 'modalOk');
          this.controller.update(annotationTypeToUpdate);
          expect(this.state.go).toHaveBeenCalledWith(
            context.viewStateName,
            { annotationTypeId: annotationTypeToUpdate.uniqueId });
        });

        it('on remove opens a modal to display annotation type in use modal', function() {
          spyOn(this.modalService, 'modalOk');
          this.controller.remove(this.annotationTypes[0]);
          expect(this.modalService.modalOk).toHaveBeenCalled();
        });

        it('can remove an annotation type', function() {
          var annotTypeToRemove = this.annotationTypes[1];
          this.controller.remove(annotTypeToRemove);
          expect(this.onRemove).toHaveBeenCalledWith(annotTypeToRemove);
        });

        it('update fails for a disabled study', function() {
          var self = this,
              StudySatus = this.$injector.get('StudyStatus');

          self.study.status = StudySatus.ENABLED();
          expect(function () {
            self.controller.update(self.annotationTypes[1]);
          }).toThrow(new Error('study is not disabled'));
        });

        it('remove fails for a disabled study', function() {
          var self = this,
              StudySatus = this.$injector.get('StudyStatus');

          self.study.status = StudySatus.ENABLED();
          expect(function () {
            self.controller.remove(self.annotationTypes[1]);
          }).toThrow(new Error('study is not disabled'));

        });

      });
    }

  });

});
