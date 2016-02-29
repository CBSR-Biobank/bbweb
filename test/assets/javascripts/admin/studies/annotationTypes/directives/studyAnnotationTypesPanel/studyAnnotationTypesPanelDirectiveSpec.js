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

  describe('Directive: studyAnnotationTypesPanelDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function () {
      this.Study                      = this.$injector.get('Study');
      this.SpecimenLinkAnnotationType = this.$injector.get('SpecimenLinkAnnotationType');
      this.AnnotationType             = this.$injector.get('AnnotationType');
      this.AnnotationValueType        = this.$injector.get('AnnotationValueType');
      this.jsonEntities               = this.$injector.get('jsonEntities');
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var self = this;

        context.study = new self.Study(self.jsonEntities.study());
        context.annotationTypes = _.map(
          self.AnnotationValueType.values(),
          function(valueType) {
            return new self.AnnotationType(
              self.jsonEntities.annotationType(context.study, { valueType: valueType }));
          });
        context.annotationTypeIdsInUse = [ context.annotationTypes[0]] ;
        context.annotationTypeName     = 'ParticipantAnnotationType';
        context.panelId                = 'study.panel.participantAnnotationTypes';
        context.addStateName           = 'home.admin.studies.study.participants.annotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        var self = this;

        context.study = new self.Study(self.jsonEntities.study());
        context.annotationTypes = _.map(
          self.AnnotationValueType.values(),
          function(valueType) {
            return new self.AnnotationType(
              self.jsonEntities.annotationType(context.study, { valueType: valueType }));
          });
        context.annotationTypeIdsInUse = [ context.annotationTypes[0] ];
        context.annotationTypeName     = 'CollectionEventAnnotationType';
        context.panelId                = 'study.panel.collectionEventAnnotationTypes';
        context.addStateName           = 'home.admin.studies.study.collection.ceventAnnotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    // describe('for Specimen Link Annotation Types', function() {
    //   var context = {};

    //   beforeEach(function () {
    //     context.study = new this.Study(this.jsonEntities.study());
    //     context.annotationTypes = _.map(
    //       this.AnnotationValueType.values(),
    //       function(valueType) {
    //       return new SpecimenLinkAnnotationType(
    //         jsonEntities.studyAnnotationType(
    //           context.study, {valueType: valueType, required: true}));
    //     });
    //     context.annotationTypeIdsInUse = [context.annotationTypes[0]];
    //     context.annotationTypeName   = 'SpecimenLinkAnnotationType';
    //     context.panelId         = 'study.panel.specimenLinkAnnotationTypes';
    //     context.addStateName    = 'home.admin.studies.study.processing.spcLinkAnnotationTypeAdd';
    //   });

    //   sharedBehaviour(context);
    // });

    function sharedBehaviour(context) {

      describe('(shared)', function() {

        beforeEach(inject(function($window,
                                   $compile,
                                   $rootScope,
                                   testUtils) {
          var element;

          this.Panel = this.$injector.get('Panel');
          this.$state = this.$injector.get('$state');

          spyOn(this.$state, 'go').and.callFake(function () {});
          spyOn(this.Panel.prototype, 'add').and.callThrough();

          $window.localStorage.setItem(context.panelId, '');

          testUtils.putHtmlTemplates(
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesPanel/studyAnnotationTypesPanel.html',
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
            '/assets/javascripts/common/directives/panelButtons.html',
            '/assets/javascripts/common/directives/updateRemoveButtons.html');


          element = angular.element([
            '<uib-accordion close-others="false">',
            '  <study-annotation-types-panel',
            '     study="vm.study"',
            '     annotation-types="vm.annotationTypes"',
            '     annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
            '     annotation-type-name="' + context.annotationTypeName + '"',
            '     panel-id="' + context.panelId + '"',
            '     add-state-name="' + context.addStateName + '"',
            '     view-state-name="' + context.viewStateName + '">',
            '  </study-annotation-types-panel>',
            '</uib-accordion>'
          ].join(''));

          this.scope = $rootScope.$new();
          this.scope.vm = {
            study:                  context.study,
            annotationTypes:        context.annotationTypes,
            annotationTypeIdsInUse: context.annotationTypeIdsInUse
          };

          $compile(element)(this.scope);
          this.scope.$digest();
          this.controller = element.find('study-annotation-types-panel')
            .controller('studyAnnotationTypesPanel');
        }));

        it('has valid scope', function () {
          expect(this.controller.study).toEqual(context.study);
          expect(this.controller.annotationTypes).toEqual(context.annotationTypes);
          expect(this.controller.annotationTypeIdsInUse).toEqual(context.annotationTypeIdsInUse);
        });

        it('has valid description', function () {
          expect(this.controller.annotationTypeDescription)
            .toContain(getDescriptionSubString(context.annotationTypeName));

          function getDescriptionSubString(annotationTypeName) {
            switch (annotationTypeName) {
            case 'ParticipantAnnotationType':
              return 'Participant';

            case 'CollectionEventAnnotationType':
              return 'Collection event';

            case 'SpecimenLinkAnnotationType':
              return 'Specimen link';

            default:
              jasmine.getEnv().fail('annotationTypeName is invalid: ' + annotationTypeName);
              return '';
            }
          }
        });

        it('should invoke panel add function', function() {
          this.controller.add();
          expect(this.$state.go).toHaveBeenCalledWith(context.addStateName);
        });

        it('should change to valid state on update', function() {
          jasmine.getEnv().fail('should change to valid state on update');
        });


      });

    }
  });

});
