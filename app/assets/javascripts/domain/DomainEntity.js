/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _   = require('lodash'),
      tv4 = require('tv4');

  DomainEntityFactory.$inject = ['UrlService'];

  /*
   * AngularJS factory.
   *
   */
  function DomainEntityFactory(UrlService) {

    /**
     * @classdesc An abstract class for an entity in the domain.
     *
     * @class
     * @memberOf domain
     *
     * @param {string[]} schema - the tv4 schema for this class.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * the derived class. Objects of this type are usually returned by the server's REST API.
     */
    function DomainEntity(schema, obj) {
      obj = obj || {};
      _.extend(this, _.pick(obj, _.keys(schema.properties)));
    }

    DomainEntity.isValid = function(schema, additionalSchemas, obj) {
      if (additionalSchemas) {
        additionalSchemas.forEach(function (schema) {
          tv4.addSchema(schema);
        });
      }

      if (!tv4.validate(obj, schema)) {
        return { valid: false, message: tv4.error.dataPath + ':' + tv4.error.message };
      }
      return { valid: true, message: null };
    };

    DomainEntity.url = function (/* pathItem1, pathItem2, ... pathItemN */) {
      return UrlService.url.apply(UrlService, _.toArray(arguments));
    };

    return DomainEntity;
  }

  return DomainEntityFactory;

});
