/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { HasAnnotationTypesMixin } from '../../annotations/HasAnnotationTypes';
import _ from 'lodash'

/* @ngInject */
function ProcessingTypeFactory($q,
                               $log,
                               biobankApi,
                               ConcurrencySafeEntity,
                               SpecimenProcessing,
                               AnnotationType,
                               DomainError) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = ConcurrencySafeEntity.createDerivedSchema({
    id: 'ProcessingType',
    properties: {
      'studyId':            { 'type': 'string' },
      'slug':               { 'type': 'string' },
      'name':               { 'type': 'string' },
      'description':        { 'type': [ 'string', 'null' ] },
      'enabled':            { 'type': 'boolean' },
      'specimenProcessing': { '$ref': 'SpecimenProcessing' },
      'annotationTypes':    { 'type': 'array', 'items': { '$ref': 'AnnotationType' }  }
    },
    required: [ 'studyId', 'slug', 'name', 'enabled', 'specimenProcessing', 'annotationTypes' ]
  });

  /**
   * @classdesc Records a regularly preformed specimen processing procedure.
   *
   * It defines and allows for recording of procedures performed on different types of {@link
   * domain.participants.Specimen Specimens}.
   *
   * For specimen processing to take place, a {@link domain.studies.Study Study} must have at least one
   * processing type defined.
   *
   * @class
   * @memberOf domain.studies
   *
   */
  class ProcessingType extends HasAnnotationTypesMixin(ConcurrencySafeEntity)  {

    /**
     * Use this contructor to create a new CollectionEventType to be persited on the server. Use {@link
     * domain.studies.ProcessingType.create|create()} or {@link
     * domain.studies.ProcessingType.asyncCreate|asyncCreate()} to create objects returned by the server.
     *
     * @param {domain.studies.Study} options.study the study this collection even type belongs to.
     *
     * @param {domain.studies.SpecimenProcessing} specimenProcessing the specimen processing information.
     *
     * @param {Array<domain.annotations.AnnotationType>} options.annotationTypes the annotation types defined
     * for this collection eventp type.
     */
    constructor(obj = {}, options = { annotationTypes: [] }) {
      /**
       * The ID of the {@link domain.studies.Study|Study} this processing type belongs to.
       *
       * @name domain.studies.ProcessingType#studyId
       * @type {string}
       */

      /**
       * A short identifying name that is unique.
       *
       * @name domain.studies.ProcessingType#name
       * @type {string}
       */

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.studies.ProcessingType#description
       * @type {string}
       * @default null
       */

      /**
       * When TRUE input specimens can be processed for this study.
       *
       * @name domain.studies.ProcessingType#enabled
       * @type {boolean}
       */

      /**
       * The specimen processing information for this processing type.
       *
       * @name domain.studies.ProcessingType#specimenProcessing
       * @type {domain.studies.SpecimenProcessing}
       */

      /**
       * The annotation types that are collected for this processing type.
       *
       * @name domain.studies.ProcessingType#annotationTypes
       * @type {Array<domain.annotations.AnnotationType>}
       */

      super(Object.assign(
        {
          studyId:     undefined,
          name:        undefined,
          description: null,
          enabled:     false
        },
        // for mixin
        {
          $q,
          biobankApi
        },
        obj));

      Object.assign(this,
                    // for mixin
                    {
                      $q,
                      biobankApi,
                      DomainError
                    },
                    _.pick(options, 'study', 'specimenProcessing', 'annotationTypes'));

      if (options.study) {
        this.studyId = options.study.id;
      }

      this.specimenProcessing = new SpecimenProcessing(obj.specimenProcessing);

      if (obj.annotationTypes) {
        this.annotationTypes =
          obj.annotationTypes.map(annotationType => new AnnotationType(annotationType));
      }
    }

    static url(...paths) {
      const allPaths = [ 'studies/proctypes' ].concat(paths);
      return super.url(...allPaths);
    }

    /**
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      const result = [ SpecimenProcessing.schema(), AnnotationType.schema() ]
            .concat(SpecimenProcessing.additionalSchemas())
            .concat(AnnotationType.additionalSchemas());
      return result;
    }

    /**
     * Creates a ProcessingType, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.ProcessingType} A processing type created from the given object.
     *
     * @see {@link domain.studies.ProcessingType.asyncCreate|asyncCreate()} when you need to create a
     * processing type within asynchronous code.
     */
    static create(obj) {
      const validation = ProcessingType.isValid(obj);
      if (!validation.valid) {
        $log.error('invalid processing type from server: ' + validation.message);
        throw new DomainError('invalid processing type from server: ' + validation.message);
      }
      return new ProcessingType(obj);
    }

    /**
     * Creates a ProcessingType from a server reply but first validates that `obj` has a valid
     * schema. *Meant to be called from within promise code*.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.studies.ProcessingType>} A processing type wrapped in a promise.
     *
     * @see {@link domain.studies.ProcessingType.create|create()} when not creating a collection event
     * type within asynchronous code.
     */
    static asyncCreate(obj) {
      try {
        const result = ProcessingType.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a ProcessingType from the server.
     *
     * @param {string} studySlug the slug of the study this processing type belongs to.
     *
     * @param {string} slug the slug of the processing type to retrieve.
     *
     * @returns {Promise<domain.studies.ProcessingType>} The processing type within a promise.
     */
    static get(studySlug, slug) {
      return biobankApi.get(ProcessingType.url(studySlug, slug))
        .then(reply => ProcessingType.asyncCreate(reply));
    }

    /**
     * Retrieves a ProcessingType from the server.
     *
     * @param {string} studyId the ID of the study this processing type belongs to.
     *
     * @param {string} id the ID of the processing type to retrieve.
     *
     * @returns {Promise<domain.studies.ProcessingType>} The processing type within a promise.
     */
    static getById(studyId, id) {
      return biobankApi.get(ProcessingType.url('id', studyId, id))
        .then(reply => ProcessingType.asyncCreate(reply));
    }

    /**
     * Fetches processing types for a {@link domain.studies.Study|Study}.
     *
     * @param {string} studySlug the slug of the study the processing types belongs to.
     *
     * @returns {Promise<common.controllers.PagedListController.PagedResult<domain.studies.ProcessingType>>}
     * A paged result containing processing types wrapped in a promise.
     */
    static list(studySlug, options = {}) {
      const url = ProcessingType.url(studySlug),
            validKeys = [
              'filter',
              'sort',
              'page',
              'limit'
            ];

      const params = _.omitBy(_.pick(options, validKeys),
                              (value) => value === '');

      return biobankApi.get(url, params).then((reply) => {
        const deferred = $q.defer();

        try {
          reply.items = reply.items.map((obj) => ProcessingType.create(obj));
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject(e);
        }
        return deferred.promise;
      });
    }

    /**
     * Sorts an array of Proecessing Types by name.
     *
     * @param {Array<domain.studies.ProcessingType>} processingTypes The array to sort.
     *
     * @return {Array<domain.studies.ProcessingType>} A new array sorted by name.
     */
    static sortByName(processingTypes) {
      return _.sortBy(processingTypes,
                      (processingType) => processingType.name);
    }

    add() {
      const json = _.pick(this, 'studyId','name', 'description', 'enabled', 'specimenProcessing');
      return biobankApi.post(ProcessingType.url(this.studyId), json)
        .then(ProcessingType.asyncCreate);
    }

    remove() {
      const url = ProcessingType.url(this.studyId, this.id, this.version);
      return biobankApi.del(url);
    }

    /**
     * @param {string} name the new name
     */
    updateName(name) {
      return this.update(ProcessingType.url('update', this.studyId, this.id),
                         { property: 'name', newValue: name });
    }

    /**
     * @param {string} description the new description
     */
    updateDescription(description) {
      const json = { property: 'description' };
      if (description) {
        json.newValue = description;
      } else {
        json.newValue = '';
      }
      return this.update(ProcessingType.url('update', this.studyId, this.id), json);
    }

    /**
     * @param {boolean} enabled the new value for enabled
     */
    updateEnabled(enabled) {
      return this.update(ProcessingType.url('update', this.studyId, this.id),
                         { property: 'enabled', newValue: enabled });
    }

    /**
     * @param {domain.studies.InputSpecimenProcessing} input
     */
    updateInputSpecimenDefinition(input) {
      return this.update(ProcessingType.url('update', this.studyId, this.id),
                         { property: 'inputSpecimenProcessing', newValue: input });
    }

    /**
     * @param {domain.studies.OutputSpecimenProcessing} output
     */
    updateOutputSpecimenDefinition(output) {
      return this.update(ProcessingType.url('update', this.studyId, this.id),
                         { property: 'outputSpecimenProcessing', newValue: output });
    }

    /**
     * @param {domain.annotations.AnnotationType} annotationType the annotation type to add
     */
    addAnnotationType(annotationType) {
      return this.update(ProcessingType.url('annottype', this.id),
                         Object.assign({ studyId: this.studyId }, _.omit(annotationType, 'uniqueId')));
    }

    /**
     * @param {domain.annotations.AnnotationType} annotationType the annotation type to update
     */
    updateAnnotationType(annotationType) {
      return this.update(ProcessingType.url('annottype', this.id, annotationType.id),
                         Object.assign({ studyId: this.studyId }, annotationType));
    }

    /**
     * @param {domain.annotations.AnnotationType} annotationType the annotation type to remove
     */
    removeAnnotationType(annotationType) {
      const url = ProcessingType.url('annottype', this.studyId, this.id, this.version, annotationType.id);
      return super.removeAnnotationType(annotationType, url)
        .then(ProcessingType.asyncCreate);
    }

    /**
     * @private
     */
    update(path, reqJson) {
      return super.update(path, reqJson).then(ProcessingType.asyncCreate);
    }

    inUse() {
      return biobankApi.get(ProcessingType.url('inuse', this.slug));
    }

  }

  return ProcessingType;
}

export default ngModule => ngModule.factory('ProcessingType', ProcessingTypeFactory)
